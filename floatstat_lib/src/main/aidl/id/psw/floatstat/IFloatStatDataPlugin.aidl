// IFloatStatDataPlugin.aidl
package id.psw.floatstat;

import id.psw.floatstat.plugins.PluginData;
/**
PARCELABLE struct PluginData {
    byte updateFlag = 0 0 0 0 0 0 0 0;
                              | | | \_ Is Text Value Updated
                              | | \___ Is Text Color Updated
                              | \_____ Is Icon Value Updated
                              \_______ Is Icon Color Updated
    HASFLAG(TEXT_VALUE) int textValueSz;
    HASFLAG(TEXT_VALUE) char16l_t textValue[textValueSz];
    HASFLAG(TEXT_COLOR) int textColor;
    HASFLAG(ICON_VALUE) int iconUriSz;
    HASFLAG(ICON_VALUE) char16l_t iconUri[iconValueSz];
    HASFLAG(ICON_COLOR) int iconColor;
}
*/

interface IFloatStatDataPlugin {
    /** Returns Data ID List for every data this plugin provides in comma-separated format,
    * the ID is comma-separated, can contain any printable ASCII value except : semi-colon (;) and slash(/)
    *
    * e.g : "now_playing_time,battery_time"
    */
    String getDataIds();
    String getPluginName();
    String getDataName(String dataId);
    PluginData getData(String dataId);
    void requestStop();
}