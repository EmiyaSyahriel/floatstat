# Contributing
## Main Project
### Translations
You can provide translation for this app by at least have a git client and a text editor.

- Clone this repository somewhere in your device
- Copy the  

## Plugin
A Plugin for this app, in it's app package, should have at least : 
- a Service which will provide every data this app can display, and should have all of 
  these in it's intent filter:
  - `id.psw.floatstat.action.START_PLUGIN` action
  - `id.psw.floatstat.category.DATA_PLUGIN` category
- a ContentProvider that can provide icon for the data which this app will request, with `exported` and `grantUriPermission` set to true
  - Android Studio will warn you about `exported=true`, but since your app have to share the data with this app, just ignore it.

### With the library
Some version release were usually go with a corresponding `floatstat_lib.aar` library file that contains
base classes for plugin creation for example : 
- `PluginData` (Parcelable data used as reply from plugins to the main app)
- `PluginIconProvider` (Base Class for Icon Provider)
- `IFloatStatDataPlugin.Stub` (Base Binder for Plugin Service, Generated via AIDL)

For further example, you can examine the source code in the `plugin_example` directory 

### Without the library
You can made a plugin service without the base library:
- Since the main app is internally using AIDL, you should know how to handle AIDL transaction with non-AIDL handler
- The specification of the `PluginData` parcelable data structure is stored in the [AIDL File](../floatstat_lib/src/main/aidl/id/psw/floatstat/IFloatStatDataPlugin.aidl)
- You should be able to provide an icon for this app using ContentProvider

### Publishing the Plugin
You are free to distribute the plugin you made in any way you want. you can made it as an open-source project,
a freeware closed-source project or even a paid app on Play Store. It's yours I have no rights controlling it.

### Tips
- You should only update your data from the plugin when needed, this is a must for the icon in particular to minimize power usage
- You should also made a shorter-text variant of the data to be shown when the overlay is in collapsed state
- The Icon Uri (`iconValue`) in the `PluginData` class should be a `content://` URI, directing to your plugin's `ContentProvider`
- When your plugin service crashes, The main app can still run without the presence of your plugin with blank line in it's place


## Financial Support
You can refer to [Donation Info](DONATE.MD) to view every possible way to donate for all contributors who 
listed theirs there.