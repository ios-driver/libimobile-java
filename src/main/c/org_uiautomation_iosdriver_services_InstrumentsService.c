/*
 * Copyright (c) 2013 Martin Szulecki. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 */

#include <jni.h>
#include "org_uiautomation_iosdriver_services_InstrumentsService.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/lockdown.h>
#include <libimobiledevice/installation_proxy.h>
#include <libimobiledevice/instruments.h>
#include <libimobiledevice/dt_message.h>

#include <plist/plist.h>

static JavaVM *jvm;

static int agent_is_ready = 0;
static int script_status = 0;
static int protocol_version = 0;

static int instruments_client_message_handler_cb(instruments_client_t client, dt_message_t message) {
	plist_t payload = NULL;
	plist_t node = NULL;

	const char* action = dt_message_get_action(message);
	const int type = dt_message_get_type(message);

	if ((type == DTMESSAGEDISPATCH) || (type == DTMESSAGEASYNCDISPATCH) && action) {
			// read script status
			if (strcmp(action, "updateScriptStatus:") == 0) {
				payload = dt_message_get_payload(message);
				node = plist_array_get_item(payload, 0);
				uint64_t status = 0;
				plist_get_uint_val(node, &status);
				script_status += status;
				node = NULL;
				payload = NULL;
			} else if (strcmp(action, "agentIsReady") == 0) {
				agent_is_ready = 1;
			} else if (strcmp(action, "agentIsGone") == 0) {
				agent_is_ready = 0;
			}

			if ((protocol_version > INSTRUMENTS_PROTOCOL_VERSION_5) && (type == DTMESSAGEASYNCDISPATCH))
				return 1;

			// build reply message
			dt_message_t reply = dt_message_new(type == DTMESSAGEDISPATCH ? DTMESSAGEREPLY: DTMESSAGEASYNCREPLY);
			dt_message_set_identifier(reply, dt_message_get_identifier(message));
			dt_message_set_priority(reply, type == DTMESSAGEDISPATCH ? 3: 4);
			dt_message_set_identifier_of_send_dispatch(reply, dt_message_get_identifier(message));
			dt_message_set_payload_with_class(reply, 4, "$null");
			const char *service_code = dt_message_get_service_code(message);
			if (service_code) {
				dt_message_set_service_code(reply, service_code);
			}

			// send reply
			if (type == DTMESSAGEDISPATCH)
				instruments_client_send_message(client, reply);
			else
				instruments_client_send_message_queued(client, reply);

			dt_message_free(reply);
			reply = NULL;

			return 1;
	} else if (dt_message_get_type(message) == DTMESSAGE) {
		payload = dt_message_get_payload(message);
		payload = plist_array_get_item(payload, 0);
		
		struct timeval timestamp = { 0, 0 };
		uint64_t mtype = 0;
		char* m = NULL;
		char* screenshot = NULL;
		uint64_t screenshot_size = 0;

		node = plist_dict_get_item(payload, "Timestamp");
		if (node && plist_get_node_type(node) == PLIST_DATE) {
			plist_get_date_val(node, (int32_t*)&timestamp.tv_sec, (int32_t*)&timestamp.tv_usec);
		}

		node = plist_dict_get_item(payload, "Type");
		if (node && plist_get_node_type(node) == PLIST_UINT) {
			plist_get_uint_val(node, &mtype);
		}

		node = plist_dict_get_item(payload, "Message");
		if (node && plist_get_node_type(node) == PLIST_STRING) {
			plist_get_string_val(node, &m);
		}

		logInfo(m);
		printf("time %ld type %d message \"%s\"\n", timestamp.tv_sec, mtype, m);

		if (m) {
			free(m);
		}

		node = NULL;
		payload = NULL;

		return 1;
	}

	return -1;
}

/*
 * Class:     org_uiautomation_iosdriver_services_InstrumentsService
 * Method:    executeScriptNative
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_InstrumentsService_executeScriptNative
  (JNIEnv * env, jobject instance, jstring uuid, jstring bundleIdentifier, jstring scriptBody, jobjectArray environment, jobjectArray arguments) {
	if (jvm == NULL) {
		int status = (*env)->GetJavaVM(env, &jvm);
		if(status != 0) {
			logError("failed storing the JVM instance.");
			return;
		}
	}

    idevice_t device = NULL;
    lockdownd_client_t lockdown = NULL;
    instruments_client_t instruments = NULL;
    lockdownd_service_descriptor_t service = NULL;
    int res = 0;

    const char *c_uuid = uuid == NULL ? NULL: (*env)->GetStringUTFChars(env, uuid, 0);
    const char *c_bundleIdentifier = (*env)->GetStringUTFChars(env, bundleIdentifier, 0);
    const char *c_scriptBody = (*env)->GetStringUTFChars(env, scriptBody, 0);

    if (IDEVICE_E_SUCCESS != idevice_new(&device, c_uuid)) {
        throwException(env, "No idevice with uuid %s found, is it plugged in?\n", c_uuid);
        return;
    }

    if (LOCKDOWN_E_SUCCESS != lockdownd_client_new_with_handshake(device, &lockdown, "java")) {
        throwException(env, "Could not connect to lockdownd. Exiting.");
        goto leave_cleanup;
    }

	service = NULL;
	if (lockdownd_start_service(lockdown, "com.apple.mobile.installation_proxy", &service) != LOCKDOWN_E_SUCCESS) {
		throwException(env, "ERROR: Could not start com.apple.mobile.installation_proxy!");
		goto leave_cleanup;
	}

	char* app_path = NULL;
	instproxy_client_t ipc = NULL;
	if (instproxy_client_new(device, service, &ipc) != INSTPROXY_E_SUCCESS) {
		throwException(env, "ERROR: Could not connect to installation_proxy!");
		goto leave_cleanup;
	}
	instproxy_client_get_path_for_bundle_identifier(ipc, c_bundleIdentifier, &app_path);
	instproxy_client_free(ipc);
	if (!app_path) {
		throwException(env, "ERROR: Could not evaluate app path for app id '%s'\n", c_bundleIdentifier);
		goto leave_cleanup;
	}
	lockdownd_service_descriptor_free(service);
	service = NULL;

	protocol_version = INSTRUMENTS_PROTOCOL_VERSION_6;
	if (lockdownd_start_service(lockdown, "com.apple.instruments.remoteserver", &service) == LOCKDOWN_E_INVALID_SERVICE) {
		logWarning("Unable to start com.apple.instruments.remoteserver!");
		if (lockdownd_start_service(lockdown, "com.apple.instruments.server", &service) != LOCKDOWN_E_SUCCESS) {
	        logError("Unable to start com.apple.instruments.server!");
	        goto leave_cleanup;
		}
		protocol_version = INSTRUMENTS_PROTOCOL_VERSION_5;
	}
	lockdownd_client_free(lockdown);
	lockdown = NULL;

    if (instruments_client_new(device, service, &instruments) != INSTRUMENTS_E_SUCCESS) {
        logError("Could not create instruments service client!");
        goto leave_cleanup;
    }
	lockdownd_service_descriptor_free(service);
	service = NULL;

    instruments_client_set_protocol_version(instruments, protocol_version);
    instruments_client_set_message_handler(instruments, &instruments_client_message_handler_cb);

    int version = -1;
	char* handle_capabilities = NULL;
	char* handle_deviceinfo = NULL;
	char* handle_processcontrol = NULL;
	char* handle_uiautomation = NULL;

    if (protocol_version == INSTRUMENTS_PROTOCOL_VERSION_6) {
		instruments_client_request_channel_with_code(instruments, "com.apple.instruments.server.services.capabilities", &handle_capabilities);
		instruments_client_request_channel_with_code(instruments, "com.apple.instruments.server.services.deviceinfo", &handle_deviceinfo);
		instruments_client_request_channel_with_code(instruments, "com.apple.instruments.server.services.processcontrol", &handle_processcontrol);
		instruments_client_version_for_capability(instruments, handle_capabilities, "com.apple.dt.services.capabilities.session", &version);
		instruments_client_request_channel_with_code(instruments, "com.apple.instruments.UIAutomation", &handle_uiautomation);
	} else {
		instruments_client_service_requested_for_identifier(instruments, "com.apple.instruments.server.services.capabilities", &handle_capabilities);
		instruments_client_service_requested_for_identifier(instruments, "com.apple.instruments.server.services.deviceinfo", &handle_deviceinfo);
		instruments_client_service_requested_for_identifier(instruments, "com.apple.instruments.server.services.processcontrol", &handle_processcontrol);
		instruments_client_service_requested_for_identifier(instruments, "com.apple.instruments.UIAutomation", &handle_uiautomation);
	}

	if (handle_uiautomation)
		instruments_client_configure_launch_environment(instruments, handle_uiautomation, "dummy.js");

	int pid = -1;
	instruments_client_process_identifier_for_bundle_identifier(instruments, handle_processcontrol, c_bundleIdentifier, &pid);

	if (pid > 0) {
		instruments_client_kill_pid(instruments, handle_processcontrol, pid);
		pid = 0;
	}

	if (pid <= 0) {
		instruments_client_launch_suspended_process_with_device_path(instruments, handle_processcontrol, app_path, c_bundleIdentifier, "dummy.js", &pid);
	}

	// give app time to launch
	sleep(2);

	instruments_client_start_observing_pid(instruments, handle_processcontrol, pid);
	instruments_client_resume_pid(instruments, handle_processcontrol, pid);

	if (handle_uiautomation)
		instruments_client_start_agent_for_app_with_pid(instruments, handle_uiautomation, c_bundleIdentifier, pid);

	if (handle_uiautomation)
		instruments_client_start_agent_for_app_with_pid(instruments, handle_uiautomation, c_bundleIdentifier, pid);

	// wait for ScriptAgent to start
	int wait_count = 10;
	while (!agent_is_ready && (wait_count-- > 0)) {
		sleep(1);
	}

	instruments_client_version_for_capability(instruments, handle_capabilities, "com.apple.instruments.UIAutomation", &version);

	if (handle_uiautomation) {
		if (version == 1) {
			// used on iOS 4.x
			instruments_client_start_script(instruments, handle_uiautomation, c_scriptBody);
		} else {
			// used on iOS 5.x
			instruments_client_start_script_with_info(instruments, handle_uiautomation, c_bundleIdentifier, c_scriptBody, "dummy.js");
		}
	}

	// wait for ScriptAgent
	wait_count = 10;
	while (handle_uiautomation && !agent_is_ready && (wait_count-- > 0)) {
		sleep(1);
	}

	// main waiting loop while a script is running
	while (agent_is_ready && script_status < 4) {
		sleep(1);
	}

	if (handle_uiautomation)
		instruments_client_stop_script(instruments, handle_uiautomation);

	instruments_client_kill_pid(instruments, handle_processcontrol, pid);
	instruments_client_stop_observing_pid(instruments, handle_processcontrol, pid);

leave_cleanup:
    if (instruments) {
        instruments_client_free(instruments);
    }
    if (lockdown) {
        lockdownd_client_free(lockdown);
    }
    if (device) {
        idevice_free(device);
    }
}
