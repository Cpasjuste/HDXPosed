package fr.mydedibox.hdxposed;

import static de.robv.android.xposed.XposedHelpers.*;

import java.lang.reflect.Method;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.XResources;
import android.net.Uri;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit
{
	@Override
	public void initZygote( StartupParam startupParam ) throws Throwable
	{
        log( "initZygote" );
        
        // play store hook
        // hook stock hdx content provider to fix play store downloads
        try
		{
        	final Class<?> cls = findClass( "android.content.ContentProviderProxy", null );
    		final Method m = findMethodExact( cls, "insert", Uri.class, ContentValues.class );
    		XposedBridge.hookMethod( m, new XC_MethodHook()
    		{
    			@Override
    			protected void beforeHookedMethod( MethodHookParam param ) throws Throwable 
    			{
    				ContentValues values = (ContentValues)param.args[1];
    				//parseContentValues( values );
    				
    				String npackage = values.getAsString( "notificationpackage" );
    				if( npackage != null && npackage.equals( "com.android.vending" ) )
    				{
    					values.put( "allowed_network_types", 3 );
    					values.put( "app_item_id_amz", 0 );
    					//values.put( "req_flags_amz", 193 );
    					values.put( "header_flags_amz", "Content-Length::Content-Type::ETag" );
    					values.put( "allow_metered", true );
    					values.put( "is_visible_in_downloads_ui", true );
    					values.put( "content_type", 1 );
    					values.put( "is_public_api", 1 );
    					values.put( "allow_roaming", true );
    					param.args[1] = values;
    				}
    			}
    		});
    		log( cls.getName()+"."+m.getName()+" hooked" );
		}
    	catch ( Throwable t ) 
    	{
    		log( t );
    	}
        
        // compareSignatures hook
        // allow unsigned apk    
        try
		{
        	final Class<?> cls = findClass("com.android.server.pm.PackageManagerService", null );
    		Method m = findMethodExact( cls, "compareSignatures", android.content.pm.Signature[].class, android.content.pm.Signature[].class );
    		XposedBridge.hookMethod( m, XC_MethodReplacement.returnConstant( 0 ) );
    		log( cls.getName()+"."+m.getName()+" hooked" );   
		}
    	catch ( Throwable t ) 
    	{
    		log( t );
    	}
        
        // wallpaper fix
        try
		{
        	XResources.setSystemWideReplacement( "android", "bool", "disable_system_wallpapers", false );
        	log( "framework-res: disable_system_wallpapers hooked" );  
		}
        catch (Throwable t) 
        {
        	log( t );
        }
        
        // ota update WIP
        try
		{
        	final Class<?> cls = findClass( "android.os.SystemProperties", null );
    		final Method m = findMethodExact( cls, "get", String.class, String.class );
    		XposedBridge.hookMethod( m, new XC_MethodHook()
    		{
    			@Override
    			protected void afterHookedMethod( MethodHookParam param ) throws Throwable 
    			{
    				String key = (String)param.args[0];
    				if( key != null 
    						&& !key.isEmpty() )
    				{
    					if( key.equals( "ro.build.version.number" ) )
    					{
    						log( "ro.build.version.number hook" );
    						param.setResult( "900000000" );
    					}
    				}
    			}
    		});
    		log( cls.getName()+"."+m.getName()+" hooked" );
		}
    	catch ( Throwable t ) 
    	{
    		log( t );
    	}
        
        // ota update WIP
        try
		{
        	final Class<?> cls = findClass( "android.content.ContextWrapper", null );
    		final Method m = findMethodExact( cls, "startService", Intent.class );
    		XposedBridge.hookMethod( m, new XC_MethodHook()
    		{
    			@Override
    			protected void beforeHookedMethod( MethodHookParam param ) throws Throwable 
    			{
    				Intent intent = (Intent)param.args[0];
    				String s = intent.toString();
    				if( s.contains( "SystemUpdatesService" ) 
    						|| s.contains( "OTAService" ) 
    						|| s.contains( "ota.ScheduledUpdateCheckerService" ) )
    				{
    					log( "startService hook: " + s );
    					param.args[0] = null;
    					param.setResult( null );
    				}
    			}
    		});
    		log( cls.getName()+"."+m.getName()+" hooked" );
		}
    	catch ( Throwable t ) 
    	{
    		log( t );
    	}
	}
	
	@Override
	public void handleLoadPackage( final LoadPackageParam lpparam ) throws Throwable 
    {
		// DownloadProvider fix (play store, gmail... downloads)
        // Prevent any call to DownloadProvider.checkInsertPermissions
		if( lpparam.packageName.equals( "com.android.providers.downloads" ) )
    	{
	    	try
			{
	    		final Class<?> cls = findClass( "com.android.providers.downloads.DownloadProvider", lpparam.classLoader );
	    		Method m = findMethodExact( cls, "checkInsertPermissions", ContentValues.class );
	    		XposedBridge.hookMethod( m, XC_MethodReplacement.returnConstant( 0 ) );
	    		log( cls.getName()+"."+m.getName()+" hooked" );  
			}
	    	catch (Throwable t) 
	    	{
	    		log( t );
	    	}
    	}
	}

	@SuppressWarnings("unused")
	private static void parseContentValues( ContentValues values )
	{
		log( "###############" );
		log( "###############" );
		for( String name : values.keySet() ) 
		{
			log( name + " : " + values.getAsString(name) );
        }
		log( "###############" );
		log( "###############" );
	}
	
	private static void log( String msg )
    {
    	XposedBridge.log( "ThorHook ===> " + msg );
    }
    
	private static void log( Throwable msg )
    {
    	XposedBridge.log( msg );
    }
}


