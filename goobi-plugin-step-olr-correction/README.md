---
description: >-
  This is technical documentation for the plugin for correcting Table Of Contents data and outputting a PICA file.
---

# Plugin for correcting Table Of Contents data and outputting a PICA file

## Introduction

This documentation describes the installation, configuration and use of the plugin.

| Details |  |
| :--- | :--- |
| Identifier | plugin_intranda_step_olr-correction |
| Source code | [https://github.com/intranda/plugin_intranda_step_olr-correction](https://github.com/intranda/plugin_intranda_step_olr-correction) |
| Licence | GPL 2.0 or newer |
| Compatibility | Goobi workflow 21.06.5 |
| Documentation date | 10.04.2021 |

### Installation

The program consists of these files:

```
plugin_intranda_step_olr-correction.jar
plugin_intranda_step_olr-correction.xml
```

The file `plugin_intranda_step_olr-correction.jar` contains the program logic, and should be copied to this path: `/opt/digiverso/goobi/plugins/step`.

The file `plugin_intranda_step_olr-correction.xml` is the config file, and should be copied to the folder `/opt/digiverso/goobi/config/`.


## Configuration

The configuration is done via the configuration file `plugin_intranda_step_olr-correction.xml` and can be adapted during operation. It is structured as follows:

```xml
<config_plugin>
    <config>
        <!-- which projects to use for (can be more then one, otherwise use *) -->
        <project>*</project>
        <step>*</step>
        <!-- which images to use -->
        <useOrigFolder>true</useOrigFolder>
        
         <!-- Are the files originally digital rather than scanned? -->
        <bornDigital>true</bornDigital>      
        
        <!-- which image sizes to use for the big image -->
        <imagesize>800</imagesize>
        <imagesize>3000</imagesize>
        <!-- which image format to generate -->
        <imageFormat>jpg</imageFormat>
        
        <!-- colors for different groups -->
        <class>
            <type>pagenum</type>
            <color>#00FFFA</color>
        </class>
        <class>
            <type>title</type>
            <color>#FF5D15</color>
        </class>
        <class>
            <type>author</type>
            <color>blue</color>
        </class>
        <class>
            <type>institution</type>
            <color>#2F8C3C</color>
        </class>
        <class>
            <type>entry</type>
            <color>grey</color>
        </class>        
    </config>
</config_plugin>
```

| Value  |  Description |
|---|---|
|   `bornDigital` |If this is true, the files are assumed to be born digital and not scanned files.   |   



### Operation of the plugin

For the current goobi process, any previously determined TOC is displayed graphically, and can be corrected. There is the option to output a PICA file.
