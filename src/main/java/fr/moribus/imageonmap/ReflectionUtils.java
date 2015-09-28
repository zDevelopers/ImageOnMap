/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;

abstract public class ReflectionUtils 
{
    static public String getBukkitPackageVersion()
    {
        return getBukkitPackageName().substring("org.bukkit.craftbukkit.".length());
    }
    
    static public String getBukkitPackageName()
    {
        return Bukkit.getServer().getClass().getPackage().getName();
    }
    
    static public String getMinecraftPackageName()
    {
        return "net.minecraft.server." + getBukkitPackageVersion();
    }
    
    static public Class getBukkitClassByName(String name) throws ClassNotFoundException
    {
        return Class.forName(getBukkitPackageName() + "." + name);
    }
    
    static public Class getMinecraftClassByName(String name) throws ClassNotFoundException
    {
        return Class.forName(getMinecraftPackageName() + "." + name);
    }
    
    static public Object getFieldValue(Class hClass, Object instance, String name) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getField(hClass, name).get(instance);
    }
    
    static public Object getFieldValue(Object instance, String name) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getFieldValue(instance.getClass(), instance, name);
    }
    
    static public Field getField(Class klass, String name) throws NoSuchFieldException
    {
        Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
    
    static public Field getField(Class klass, Class type) throws NoSuchFieldException
    {
        for(Field field : klass.getDeclaredFields())
        {
            if(field.getType().equals(type))
            {
                field.setAccessible(true);
                return field;
            }
        }
        throw new NoSuchFieldException("Class " + klass.getName() + " does not define any field of type " + type.getName());
    }
    
    static public void setFieldValue(Object instance, String name, Object value) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        setFieldValue(instance.getClass(), instance, name, value);
    }
    
    static public void setFieldValue(Class hClass, Object instance, String name, Object value) 
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        getField(hClass, name).set(instance, value);
    }
    
    static public Object call(Class hClass, String name, Object ... parameters) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return call(hClass, null, name, parameters);
    }
    
    static public Object call(Object instance, String name, Object ... parameters) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        return call(instance.getClass(), instance, name, parameters);
    }
    
    static public Object call(Class hClass, Object instance, String name, Object ... parameters) 
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method method = hClass.getMethod(name, getTypes(parameters));
        return method.invoke(instance, parameters);
    }
    
    static public Object instanciate(Class hClass, Object ... parameters) 
            throws NoSuchMethodException, InstantiationException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Constructor constructor = hClass.getConstructor(getTypes(parameters));
        return constructor.newInstance(parameters);
    }
    
    static public Class[] getTypes(Object[] objects)
    {
        Class[] types = new Class[objects.length];
        for(int i = 0; i < objects.length; i++)
        {
            types[i] = objects[i].getClass();
        }
        return types;
    }
}