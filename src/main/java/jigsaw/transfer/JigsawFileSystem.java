package jigsaw.transfer;

import fr.insalyon.creatis.grida.client.*;
import fr.insalyon.creatis.grida.common.bean.Operation;
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
    private GRIDAPoolClient gridaPoolClient;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public JigsawFileSystem(File proxy, String vo) {
        Configuration conf = Configuration.INSTANCE;
        gridaClient = new GRIDAClient(
                conf.getGridaHost(),
                conf.getGridaPort(),
                conf.getServerProxy());
        gridaPoolClient = new GRIDAPoolClient(
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
                 String sourceFileName = new File(source.getPath()).getName();
                 // dfc grida wants the destination folder
                 String operationId = gridaPoolClient.downloadFile(source.getPath(), destinationFolder, null);
                 // asynchronous download, but the method is used synchronously
                 // So we need to wait until it's over
                 waitForDownload(operationId);
                 // we need to rename it, in case the destination file name is different from the source one
                 boolean renameSuccess = new File(destinationFolder, sourceFileName).renameTo(destinationFile);
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

    // #### Operation stuff (copied from vip-api)

    private void waitForDownload(String operationId) throws JigsawException {
        Callable<Boolean> isDownloadOverCall = () -> isOperationOver(operationId);

        Configuration conf = Configuration.INSTANCE;
        Integer retryInSeconds = conf.getDownloadRetryInSec();
        Long timeoutInSeconds = conf.getDownloadTimeoutInSec();

        System.out.println("In moteur2-grida : checking every " + retryInSeconds + " seconds if the download is over");
        System.out.println("In moteur2-grida : or waiting a timeout of : " + timeoutInSeconds);
        // task that check every x seconds if the operation is over.
        // return true when OK or goes on indefinitely
        Callable<Boolean> waitForDownloadCall = () -> {
            while (true) {
                Future<Boolean> isDownloadOverFuture =
                        scheduler.schedule(isDownloadOverCall,
                                retryInSeconds, TimeUnit.SECONDS);
                if (isDownloadOverFuture.get()) {
                    return true;
                }
            }
        };
        // launch the checking task, then wait until the timeout
        Future<Boolean> completionFuture =
                scheduler.submit(waitForDownloadCall);
        timeoutOperationCompletionFuture(operationId, completionFuture, timeoutInSeconds);
    }

    private void timeoutOperationCompletionFuture (
            String operationId,
            Future<Boolean> completionFuture,
            long timeoutInSeconds) throws JigsawException {
        try {
            // if the future (and the download) is over before the timoue, the method is finished
            // otherwise a TimeoutException is thrown
            completionFuture.get(timeoutInSeconds, TimeUnit.SECONDS);
            System.out.println("In moteur2-grida : download over");
        } catch (InterruptedException e) {
            System.err.println("Waiting for operation completion interrupted :" + operationId);
            throw new JigsawException("Waiting for operation completion interrupted", e);
        } catch (ExecutionException e) {
            System.err.println("Error waiting for operation completion :" + operationId);
            throw new JigsawException("Error waiting for operation completion", e);
        } catch (TimeoutException e) {
            // timeout reached : kill the download task or it will go on indefinitely
            completionFuture.cancel(true);
            System.err.println("Timeout with download operation :" + operationId);
            throw new JigsawException("Aborting operation : too long", e);
        }
    }

    private boolean isOperationOver(String operationId) throws GRIDAClientException, JigsawException {
        Operation operation = gridaPoolClient.getOperationById(operationId);

        switch (operation.getStatus()) {
            case Queued:
            case Running:
                return false;
            case Done:
                return true;
            case Failed:
            case Rescheduled:
            default:
                System.err.println("Grida download failed : " + operationId + " : " + operation.getStatus());
                throw new JigsawException("Grida download failed");
        }
    }

}
