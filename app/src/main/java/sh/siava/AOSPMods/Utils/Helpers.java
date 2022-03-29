package sh.siava.AOSPMods.Utils;

import com.topjohnwu.superuser.Shell;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Helpers {

    public static List<String> activeOverlays = null;

    public static void dumpClass(String className, XC_LoadPackage.LoadPackageParam lpparam)
    {
        Class ourClass = XposedHelpers.findClassIfExists(className, lpparam.classLoader);
        if(ourClass == null)
        {
            XposedBridge.log("Class: " + className + " not found");
            return;
        }
        Method[] ms = ourClass.getDeclaredMethods();
        XposedBridge.log("Class: " + className);
        XposedBridge.log("Methods:");

        for(Method m : ms)
        {
            XposedBridge.log(m.getName() + " - " + m.getReturnType() + " - " + m.getParameterCount());
            Class[] cs = m.getParameterTypes();
            for(Class c: cs)
            {
                XposedBridge.log("\t\t" + c.getTypeName());
            }
        }
        XposedBridge.log("Fields:");

        Field[] fs = ourClass.getDeclaredFields();
        for(Field f: fs)
        {
            XposedBridge.log("\t\t" + f.getName() + "-" + f.getType().getName());
        }
        XposedBridge.log("End dump");
    }

    public static void getActiveOverlays()
    {
        List<String> result = new ArrayList<>();
        List<String> lines = Shell.su("cmd overlay list --user 0").exec().getOut();
        for(String thisLine : lines)
        {
            if(thisLine.startsWith("[x]"))
            {
                result.add(thisLine.replace("[x] ", ""));
            }
        }
        activeOverlays = result;
    }
    public static void setOverlay(String Key, boolean enabled, boolean refresh) {
        if(refresh) getActiveOverlays();
        setOverlay(Key, enabled);
    }

    public static void setOverlay(String Key, boolean enabled) {
        if(activeOverlays == null) getActiveOverlays(); //make sure we have a list in hand

        String mode = (enabled) ? "enable" : "disable";
        String packname;
        boolean exclusive = false;

        if(Key.endsWith("Overlay")) {
            Overlays.overlayProp op = (Overlays.overlayProp) Overlays.Overlays.get(Key);
            packname = op.name;
            exclusive = op.exclusive;
        }
        else
        {
            packname = Key;
            exclusive = true;
        }

        if (enabled && exclusive) {
            //mode += "-exclusive"; //since we are checking all overlays, we don't need exclusive anymore.
        }

        boolean wasEnabled = (activeOverlays.contains(packname));

        try
        {
            XposedBridge.log("pack is to:" + packname + wasEnabled + enabled);
        }catch(Throwable e){}

        if(enabled == wasEnabled)
        {
            return; //nothing to do
        }
        try
        {
            XposedBridge.log("action:" + packname + mode);
            XposedBridge.log("cmd:" + "cmd overlay " + mode + " --user 0 " + packname);
        }catch(Throwable e){}


        try {
            Shell.su("cmd overlay " + mode + " --user 0 " + packname).exec();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
}
