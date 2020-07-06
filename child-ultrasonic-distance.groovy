/**
 *  Child Ultrasonic Sensor for Distance
 *
 *  https://raw.githubusercontent.com/zedly99/Hubitat/master/child-ultrasonic-distance.groovy
 *
 *  Copyright 2017 Daniel Ogorchock, modifications by SteveZed
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2018-06-02  Dan Ogorchock  Revised/Simplified for Hubitat Composite Driver Model
 *    2018-09-22  Dan Ogorchock  Added preference for debug logging
 *    2019-07-01  Dan Ogorchock  Added importUrl
 *    2020-01-25  Dan Ogorchock  Remove custom lastUpdated attribute & general code cleanup
 *    2020-07-01  Steve Zed      Modified to show presence based on distance
 *
 * 
 */
metadata {
	definition (name: "Child Ultrasonic Sensor", namespace: "zedly99", author: "SteveZed", importUrl: "https://raw.githubusercontent.com/zedly99/Hubitat/master/child-ultrasonic-distance.groovy") {
	capability "Sensor"
        capability "Presence Sensor"
        attribute "distance", "Number"
    }


    preferences {
        input name: "distance", type: "number", title: "Distance", description: "Maximum distance to register as present (cm)", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}


def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}


def parse(String description) {
    if (logEnable) log.debug "parse(${description}) called"
	def parts = description.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    if (name && value) {
        double sensorValue = value as float
        sensorValue = sensorValue.round(2)
        sendEvent(name: "distance", value: sensorValue)
        //Determine if this distance means presence or not
        if (sensorValue <= distance) {
            if (device.currentValue("presence") != "present") {
                sendEvent(name: "presence", value: "present", isStateChange: true, descriptionText: "New update received from HubDuino device")
            }
        } else {
            if (device.currentValue("presence") != "not present") {
                sendEvent(name: "presence", value: "not present", isStateChange: true, descriptionText: "New update received from HubDuino device") 
            }
        }
    }
    else {
    	log.error "Missing either name or value.  Cannot parse!"
    }
}


def installed() {
    updated()
}


def updated() {
    if (logEnable) runIn(1800,logsOff)
}
