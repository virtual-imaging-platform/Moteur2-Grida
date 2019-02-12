package jigsaw.transfer;

import fr.insalyon.creatis.grida.client.*;
import fr.insalyon.creatis.moteur2.Configuration;
import jigsaw.exception.JigsawException;

import java.io.File;
import java.net.URI;
import java.util.concurrent.*;

/**
 * Created by abonnet on 1/30/19.
 *
 * Package and class must not change, that overides an existing class used in moteur.
 * This has been done to override the original behaviour that used the not supported
 * anymore lcg utils.
 *
 */
public class JigsawFileSystem {

    private GRIDAClient gridaClient;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public JigsawFileSystem(File proxy, String vo) {
        Configuration conf = Configuration.INSTANCE;
        gridaClient = new GRIDAClient(
                conf.getGridaHost(),
                conf.getGridaPort(),
                conf.getServerProxy());
        System.out.println("Using grida from moteur");
        System.out.println("Grida host : " + conf.getGridaHost());
        System.out.println("Grida port : " + conf.getGridaPort());
        System.out.println("Grida proxy : " + conf.getServerProxy());
    }

    /*
        Used in moteur to check if the gasw file has been updated
     */
    public long getModificationTime(URI file) throws JigsawException {
        try {
            return gridaClient.getModificationDate(file.getPath());
        } catch (GRIDAClientException e) {
            System.err.println("Grida error " + e);
            throw new JigsawException("Grida error : " + e, e);
        }
    }

     public void copy(URI source, URI destination) throws JigsawException {
         System.out.println("In moteur2-grida : transferring " + source + " to " + destination);
         if ("lfn".equals(source.getScheme()) && "file".equals(destination.getScheme())) {
             try {
                 File destinationFile = new File(destination.getPath());
                 String destinationFolder = destinationFile.getParent();
                 String localFile = gridaClient.getRemoteFile(source.getPath(), destinationFolder);
                 // synchronous download, no need to wait
                 // we need to rename it, in case the destination file name is different from the source one
                 boolean renameSuccess = new File(localFile).renameTo(destinationFile);
                 if (! renameSuccess) {
                     System.err.println("Error renaming gasw file");
                     throw new JigsawException("Error renaming gasw file");
                 }
             } catch (GRIDAClientException e) {
                 System.err.println("Grida error " + e);
                 throw new JigsawException("Grida error : " + e, e);
             }
         } else {
             System.err.println("Only lfn > file transfers are supported");
             System.err.println("source : " + source);
             System.err.println("destination :" + destination);
             throw new JigsawException("Only lfc > file transfers are supported");
         }
         System.out.println("In moteur2-grida : transferring successful");
     }
}
