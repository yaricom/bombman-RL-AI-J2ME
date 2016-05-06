****************************************************
*** J2ME Polish 1.2.4
****************************************************

Find more information about J2ME Polish at www.j2mepolish.org

The documentation is located in the doc-folder.

To migrate J2ME Polish into existing projects, just copy the file 
"build.xml" and the "import" folder to the project root. 

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.
(http://http://www.opensource.org).

Commercial licenses for closed source software are also available,
please check http://www.j2mepolish.org.


****************************************************
*** Sample Application 
****************************************************

If you have installed the sample application, you can now
build your MIDlet right away. Just call "ant" in the installation
folder (this folder). J2ME Polish will create the MIDlet files in
the "dist" folder.
When you call "ant test j2mepolish" the obfuscation step will
be skipped and the logging will be enabled.

You can change the design of the sample application by changing
the file "resources/polish.css". You can check out an entirely
different design by changing the "resdir"-attribute in the 
"build.xml" file to "resources2" (instead of "resources").

Have fun!
