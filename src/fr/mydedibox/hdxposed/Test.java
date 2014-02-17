package fr.mydedibox.hdxposed;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;

import java.lang.reflect.Method;

import android.graphics.Canvas;
import android.graphics.Rect;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class Test {

	public static void HookResources()
	{
		try
		{
        	final Class<?> cls = findClass( "android.content.res.Resources", null );
    		final Method m = findMethodExact( cls, "lockCanvas", Rect.class );
    		XposedBridge.hookMethod( m, new XC_MethodHook()
    		{
    			@Override
    			protected void beforeHookedMethod( MethodHookParam param ) throws Throwable 
    			{
    				
    			}
    		});
    		log( cls.getName()+"."+m.getName()+" hooked" );
		}
    	catch ( Throwable t ) 
    	{
    		log( t );
    	}
	}
	
	public static void HookSurface()
	{
		try
		{
        	final Class<?> cls = findClass( "android.view.Surface", null );
    		final Method m = findMethodExact( cls, "lockCanvas", Rect.class );
    		XposedBridge.hookMethod( m, new XC_MethodReplacement()
    		{
    			Canvas canvas = null;
    			
				@Override
				protected Object replaceHookedMethod( MethodHookParam param )
				{
					Rect rect = (Rect)param.args[0];
					
					log( "lockCanvas: rect = " + rect );
					
					if( canvas != null )
						return canvas;
					
					Canvas c = null;
					try 
					{
						c = (Canvas)callMethod( param.thisObject, "nativeLockCanvas", rect );
					}
					catch( Exception e )
					{
						log( e.getCause() );
					}
				
					if( c != null )
					{
						log( "lockCanvas: success" );
						canvas = c;
						return canvas;
					}
					else
					{
						log( "lockCanvas: error" );
						if( canvas != null )
						{
							//log( "lockCanvas: hooking.." );
							//callMethod( param.thisObject, "nativeUnlockCanvasAndPost", canvas );
							//return (Canvas)callMethod( param.thisObject, "nativeLockCanvas", rect );
							return canvas;
						}
					}
					return null;
				}
    			
    		});
    		log( cls.getName()+"."+m.getName()+" hooked" );
		}
    	catch ( Throwable t ) 
    	{
    		log( t );
    	}
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
