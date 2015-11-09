/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sakit
 */
public class WindowsRegistry {

    private static final String MASK = "    ";
    private static final String COMMAND = "reg query \"";

    public static final Registry[] readRegistry(String location) {

        location = COMMAND + location + "\"";
        System.out.println(location);
        String[] result = executeCommand(location);
        List<Registry> list = new ArrayList<>();
        if (result != null) {
            String path = result[0].trim();
            
            for (int i = 1; i < result.length; i++) {
                String[] tmp = result[i].split(MASK);
                Registry reg = new Registry(tmp[1], tmp[2], tmp[3]);
                reg.setPath(path);
                list.add(reg);
            }
        }
        return list.isEmpty()? null:list.toArray(new Registry[list.size()]);
    }

    private static String[] executeCommand(String command) {

        List<String> output = new ArrayList<>();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.equals("\n")) {
                    continue;
                }
                output.add(line);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        return output.isEmpty() ? null : output.toArray(new String[output.size()]);
    }

    public static boolean isRegistryEnable(String[] paths, String name) {

        String[] result = null;
        for (String p : paths) {
            String command = "reg query \"" + p + "\" /v " + name;
            result = executeCommand(command);
            if (result != null) {
                String[] s = result[1].split(MASK);
                return s[3].equals("020000000000000000000000");
            }
        }
        return false;
    }
}
