/**
 *  Chapman Fibaro RGBW Virtual Routine Switch
 *
 *  Copyright 2016 chapmanportal
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
 */
definition(
    name: "Fibaro RGBW Color Virtual Routine Switch",
    namespace: "chapmanportal/smartthings",
    author: "chapmanportal",
    description: "Virtual Switch to set light colour when triggered by routine execution",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {

page(name: "pageOne", title: "Configure your Lighting", nextPage: "selectRoutines", uninstall: true) {

	section("Configure your Colour Switch:") {		
        input "ledlight","capability.colorControl",title: "Choose your RGBW Light",required: true,multiple: true;        
        input "onOff","enum",title: "Turn On or Off?", options: ["On","Off"],required: false,multiple: false;
        input "pattern1", "enum", title: "Pick a Color / Pattern", description: "Select the pattern to use", options: ["daylight","red","green","blue","cyan","magenta","orange","purple","yellow","white","fireplace","storm","deepfade","litefade","police"], multiple: false, required: true;        
        input "level", "enum", title: "Set Dimmer Level", options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]], defaultValue: "100"     
        label title: "Assign a name", required: false        
	}    
}   
    page(name: "selectRoutines")    
}
    
def selectRoutines() {
    dynamicPage(name: "selectRoutines", title: "Select Routine to Execute", install: true, uninstall: true) {

        // get the available list of routines
        def routines = location.helloHome?.getPhrases()*.label
        if (routines) {
            // sort them alphabetically
            routines.sort()
            section("Select a Routine") {
                log.trace routines
                // use the routines list as the options for an enum input
                input "routines", "enum", title: "Select a routine to trigger lighting", options: routines,multiple:true;
            }
        }
    }
}
   


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	// Listen for routines that have been executes
    subscribe(location, "routineExecuted", routineChanged)    
}

def routineChanged(evt) {

    log.debug "rountineChanged, triggerRoutine = $routines, location.routineExecuted = $evt.displayName, pattern = $pattern1, onOff = $onOff"
	
    
if (routines.contains(evt.displayName) {
    	//If Light is to be turned on, Set LED colour and brightness level.  If Fibaro light program is selected, then bypass level setting (this reverts light back to LED colour and cancels light program)
    	if (onOff == "On") {
        	log.debug"Setting Colour to $pattern1"
        	ledlight."$pattern1"()
            if (pattern1 != "police" && pattern1 != "fireplace" && pattern1 != "deepfade" && pattern1 != "litefade") {
            	log.debug"Setting Level to $level"
            	ledlight.setLevel(level as Integer)
            }
            //If color set to "daylight" use RGB white and not White LED
            if (pattern1 == "daylight") {
            	ledlight.setColor(["hex":"#ffffff"])
            	log.debug"Setting Level to $level"
            	ledlight.setLevel(level as Integer)
                ledlight.setWhiteLevel(0)
            }
        } else {
        	//Revert colour configuration back to White LED then turn off light
        	log.debug"Setting Colour to $pattern1 and turning Off"
        	ledlight."$pattern1"()
            log.debug"Setting Light Color to White"
            ledlight.setColor(["hex":"#ffffff"])
            ledlight.setWhiteLevel(0)
        	log.debug"Turn Off"
        	ledlight.off()
        }
    }
}
