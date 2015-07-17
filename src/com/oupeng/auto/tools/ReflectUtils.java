package com.oupeng.auto.tools;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;


public class ReflectUtils {
    private static final String TAG = OupengConfig.logFliterTag;

    /**
     * Instantiate an object of specified class.
     * It is possible to instantiate an object of hidden class.
     *
     * @param className name of the class
     * @param params params for the method
     * @param paramTypes type of params. It will try to get the type from params if paramTypes is null.
     * @return return value of the invoked method.
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiateClassObject(String className, Object[] params,
            Class<?>[] paramTypes) {
        Class<?>[] types = null;
        if (paramTypes != null) {
            types = paramTypes;
        } else if (params != null) {
            types = new Class<?>[params.length];
            for (int i = 0; i < params.length; ++i) {
                types[i] = (params[i] == null) ? null : params[i].getClass();
            }
        }

        Object ret = null;
        try {
            Class<?> classObj = null;
            Constructor<?> c = null;
            classObj = Class.forName(className);
            if (classObj != null) {
                c = classObj.getConstructor(types);
            }
            if (c != null) {
                c.setAccessible(true);
                ret = c.newInstance(params);
            }
        } catch (ClassNotFoundException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "ClassNotFoundException: " + "className: " + className);
            }
        } catch (NoSuchMethodException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "NoSuchMethodException: " + "className: " + className);
            }
        } catch (IllegalArgumentException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "IllegalArgumentException: " + "className: " + className);
            }
        } catch (InstantiationException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "InstantiationException: " + "className: " + className);
            }
        } catch (IllegalAccessException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "IllegalAccessException: " + "className: " + className);
            }
        } catch (InvocationTargetException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "InvocationTargetException: " + "className: " + className);
            }
        }

        return ret == null ? null : (T) ret;
    }

    /**
     * Find and invoke the static method of specified class if it exists.
     * It is possible to invoke a static method of a hidden class.
     *
     * @param className name of the class
     * @param methodName name of the method to be invoked
     * @param params params for the method
     * @param paramTypes type of params. It will try to get the type from params if paramTypes is null.
     * @return return value of the invoked method.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeClassStaticMethod(String className, String methodName,
            Object[] params,
            Class<?>[] paramTypes) {
        if (className == null || methodName == null) {
            return null;
        }
        Class<?> classObject = null;
        try {
            classObject = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "ClassNotFoundException: " + "className: " + className);
            }
        }
        if (classObject != null) {
            return invokeMethod(classObject, methodName, params, paramTypes);
        }
        return null;
    }

    /**
     * Get the static field of specified class if it exists.
     *
     * @param className name of the class
     * @param fileName name of the field
     * @return return value of found field.
     */
    public static Object getClassStaticField(String className, String fieldName) {
        if (className == null || fieldName == null) {
            return null;
        }
        Class<?> classObject = null;
        try {
            classObject = Class.forName(className);
        } catch (ClassNotFoundException e) {
            if (OupengConfig.DEBUG) {
                Log.d(TAG, "ClassNotFoundException: " + "className: " + className);
            }
        }
        if (classObject != null) {
            return getFieldValue(classObject, fieldName, null);
        }
        return null;
    }

    /**
     * Find and invoke the method on the object if it exists.
     * Do not attempt to invoke a method with a signature that does not exist.
     * It is possible to invoke protected or private method by this function.
     *
     * @param class or object instance of class
     * @param methodName name of the method to be invoked
     * @param params params for the method
     * @param paramTypes type of params. It will try to get the type from params if paramTypes is null.
     * @return return value of the invoked method.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object object, String methodName, Object[] params,
            Class<?>[] paramTypes) {
        if (object == null) {
            return null;
        }

        Class<?>[] types = null;

        if (paramTypes != null) {
            types = paramTypes;
        } else if (params != null) {
            types = new Class<?>[params.length];
            for (int i = 0; i < params.length; ++i) {
                types[i] = (params[i] == null) ? null : params[i].getClass();
            }
        }

        Method method = getMethodEx(object, methodName, types);
        // invoke
        if (method != null) {
            method.setAccessible(true);
            try {
                return object instanceof Class ? (T) method.invoke(null, params) : (T) method
                        .invoke(object, params);
            } catch (IllegalAccessException iae) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "IllegalAccessException: " + "invokeMethod " + methodName);
                }
            } catch (IllegalArgumentException iage) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "IllegalArgumentException: " + "invokeMethod " + methodName);
                }
            } catch (InvocationTargetException ite) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "InvocationTargetException: " + "invokeMethod " + methodName);
                }
            } catch (ExceptionInInitializerError eiie) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "ExceptionInInitializerError: " + "invokeMethod " + methodName);
                }
            } catch (ClassCastException cce) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "ClassCastException: " + "invokeMethod " + methodName);
                }
                assert false;
                throw cce;
            }
        }

        assert false;
        return null;
    }

    /**
     * Get specified field value of object
     *
     * @param object instance of class
     * @param fieldName name of the field to be get
     * @return field value of object
     */
    @SuppressWarnings("unchecked")
    private static <T> T getFieldValueInternal(Object object, String fieldName) {
        if (object == null) {
            return null;
        }

        Field field = getFieldEx(object, fieldName);

        if (field != null) {
            field.setAccessible(true);
            try {
                return (T) field.get(object);
            } catch (IllegalAccessException iae) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "IllegalAccessException: " + "getFieldValue " + fieldName);
                }
            } catch (IllegalArgumentException iae) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "IllegalArgumentException: " + "getFieldValue " + fieldName);
                }
            } catch (ExceptionInInitializerError eiie) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "ExceptionInInitializerError: " + "getFieldValue " + fieldName);
                }
            } catch (ClassCastException cce) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "ClassCastException: " + "getFieldValue " + fieldName);
                }
                assert false;
                throw cce;
            }
        }

        assert false;
        return null;
    }

    /**
     * Get specified field value of object
     *
     * @param object instance of class
     * @param fieldName name of the field to be get
     * @param defaultValue value returned if field value is null.
     * @return field value of object if not null, otherwise default value will be returned
     */
    public static <T> T getFieldValue(Object object, String fieldName, T defaultValue) {
        T val = getFieldValueInternal(object, fieldName);
        return val != null ? (T)val : (T)defaultValue;
    }

    /**
     * Set specified field value of objcet
     *
     * @param object instance of class
     * @param fieldName name of the field to be set
     * @param value field value to be set
     * @return true if field value was set successfully.
     */
    public static boolean setFieldValue(Object object, String fieldName, Object value) {
        if (object == null) {
            return false;
        }

        Field field = getFieldEx(object, fieldName);

        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(object, value);
                return true;
            } catch (IllegalAccessException iae) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "IllegalAccessException: " + "setFieldValue " + fieldName);
                }
            } catch (ExceptionInInitializerError eiie) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "ExceptionInInitializerError: " + "setFieldValue " + fieldName);
                }
            }
        }

        assert false;
        return false;
    }

    /**
     * Get specified field value of objcet or class.
     * If field is not found in current class, then get it from super class.
     *
     * @param object instance of class or class
     * @param fieldName name of the field to be get
     * @return field value of object
     */
    static Field getFieldEx(Object object, String fieldName) {
        Field field = null;

        Class<?> theClass = object instanceof Class ? (Class<?>) object : object.getClass();
        for (; theClass != Object.class; theClass = theClass.getSuperclass()) {
            try {
                field = theClass.getDeclaredField(fieldName);
                return field;
            } catch (NoSuchFieldException e) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "NoSuchFieldException: " + theClass.getName() + "." + fieldName);
                }
            } catch (SecurityException e) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "SecurityException: " + theClass.getName() + "." + fieldName);
                }
            }
        }

        Log.e(TAG, "getFieldEx: " + object.getClass().getName() + "." + fieldName + " not found");
        assert false;
        return null;
    }

    /**
     * Get specified method of object or class.
     * If method is not found in current class, then get it from super class.
     *
     * @param object instance of class or class
     * @param methodName name of the method to be get
     * @param paramTypes type of params
     * @return method of object
     */
    private static Method getMethodEx(Object object, String methodName, Class<?>[] paramTypes) {
        Method method = null;
        Class<?> theClass = object instanceof Class ? (Class<?>) object : object.getClass();
        for (; theClass != Object.class; theClass = theClass.getSuperclass()) {
            try {
                method = theClass.getDeclaredMethod(methodName, paramTypes);
                return method;
            } catch (NoSuchMethodException e) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "NoSuchMethodException: " + theClass.getName() + "." + methodName);
                }
            } catch (SecurityException e) {
                if (OupengConfig.DEBUG) {
                    Log.d(TAG, "SecurityException: " + theClass.getName() + "." + methodName);
                }
            }
        }

        Log.e(TAG, "getMethodEx: " + object.getClass().getName() + "." + methodName + " not found");
        assert false;
        return null;
    }
}

