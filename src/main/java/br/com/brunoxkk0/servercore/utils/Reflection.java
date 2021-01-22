package br.com.brunoxkk0.servercore.utils;

import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;

public class Reflection {

    private final String targetPackage;
    private final ClassLoader classLoader;

    private final boolean ignoreInterfaces;
    private final boolean ignoreAnnotations;

    private final HashSet<Class<?>> classes;

    Reflection(String targetPackage, ClassLoader classLoader, boolean ignoreInterfaces, boolean ignoreAnnotations){

        if(targetPackage != null){

            this.targetPackage = targetPackage;
            this.classLoader = classLoader;
            this.ignoreAnnotations = ignoreAnnotations;
            this.ignoreInterfaces = ignoreInterfaces;

            classes = new HashSet<>();
            return;
        }

        throw new NullPointerException("targetPackage can't be null");
    }

    public static ReflectionBuilder builder(){
        return new ReflectionBuilder();
    }

    public HashSet<Class<?>> scan(){

        ClassLoader cl = (classLoader != null) ? classLoader : getCurrentClassLoader();

        Enumeration<URL> data = null;

        try {
            data = cl.getResources(packageToPath(targetPackage));
        } catch (IOException e) { e.printStackTrace(); }

        if(data != null && data.hasMoreElements()){

            URL url = data.nextElement();
            URLConnection connection = null;

            while (url != null){

                try {
                    connection = url.openConnection();
                }
                catch (IOException ignored) {

                    if(data.hasMoreElements()){
                        url = data.nextElement();
                        continue;
                    }

                }

                if(connection != null){

                    if(connection instanceof JarURLConnection){

                        try {
                            handleJar((JarURLConnection) connection, targetPackage);
                        } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }

                    } else if (connection instanceof FileURLConnection){

                        try {
                            handleFiles((FileURLConnection) connection);
                        } catch (IOException e) { e.printStackTrace(); }

                    }
                }

                if(data.hasMoreElements()){

                    url = data.nextElement();

                    continue;
                }

                url = null;
            }
        }

        return classes;
    }

    private String packageToPath(String name){
        return name.replace(".","/");
    }

    private String pathToPackage(String name){

        if(name.contains("java\\main")){
            name = name.substring(name.indexOf("java\\main") + 10);
        }

        return name.replace("\\",".");
    }

    private String normalize(String name){
        return name.substring(0, name.length() - 6).replace("/",".");
    }

    private String noClass(String name){
        return name.substring(0, name.length() - 6);
    }

    private ClassLoader getCurrentClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }

    private void handleJar(JarURLConnection connection, String targetPackage) throws IOException, ClassNotFoundException {

        Enumeration<JarEntry> enumeration = connection.getJarFile().entries();

        JarEntry entry;
        while (enumeration.hasMoreElements()){

            entry = enumeration.nextElement();

            if(entry.getName().contains(".class")){

                String name = normalize(entry.getName());

                if(name.startsWith(targetPackage)){
                    Class<?> clazz = Class.forName(name);

                    if(!((clazz.isAnnotation() && ignoreAnnotations) || (clazz.isInterface() && ignoreInterfaces))){
                        classes.add(clazz);
                    }

                }

            }
        }
    }

    private void handleFiles(FileURLConnection connection) throws IOException {

        File dir = new File(URLDecoder.decode(connection.getURL().getPath(), "UTF-8"));

        Iterator<Path> paths = Files.walk(dir.toPath()).iterator();

        Path path;
        while(paths.hasNext()){

            path = paths.next();

            if(path.toString().endsWith(".class")){
                String fName = noClass(path.getFileName().toString());

                String fPackage = pathToPackage(path.getParent().toString());

                try {

                    Class<?> clazz = Class.forName(fPackage + '.' + fName);

                    if(!((clazz.isAnnotation() && ignoreAnnotations) || (clazz.isInterface() && ignoreInterfaces))){
                        classes.add(clazz);
                    }

                } catch (NoClassDefFoundError | ClassNotFoundException ignored) {}
            }
        }
    }

    public static class ReflectionBuilder {

        private String targetPackage;
        private ClassLoader classLoader;
        private boolean ignoreInterfaces = true;
        private boolean ignoreAnnotations = true;

        public ReflectionBuilder setClassLoader(ClassLoader classLoader){
            this.classLoader = classLoader;

            return this;
        }

        public ReflectionBuilder setPackage(String targetPackage){
            this.targetPackage = targetPackage;

            return this;
        }

        public ReflectionBuilder ignoreInterfaces(boolean ignoreInterfaces){
            this.ignoreInterfaces = ignoreInterfaces;

            return this;
        }
        
        public ReflectionBuilder ignoreAnnotations(boolean ignoreAnnotations){
            this.ignoreAnnotations = ignoreAnnotations;

            return this;
        }

        public Reflection build(){
            return new Reflection(targetPackage, classLoader, ignoreInterfaces, ignoreAnnotations);
        }

    }

}