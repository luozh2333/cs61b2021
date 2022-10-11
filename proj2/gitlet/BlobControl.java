package gitlet;

import java.io.File;

import static gitlet.Utils.*;

public class BlobControl {
    public static final File BLOB_DIR = join(Repository.GITLET_DIR, "blobs");

    public static void addBlob(String name, StagingArea SA, Commit thisCommit) {
        File toBeAdd = join(Repository.CWD, name);
        if (!toBeAdd.exists()) {
            Utils.exit("File does not exist.");
        }
        byte[] content = readContents(toBeAdd);
        String BlobName = sha1(content);
         /* If the current working version of the file is identical to the version
        in the current commit, do not stage it to be added, and remove it from the
        staging area if it is already there */
        if (thisCommit.GetReferenceID(name) != null &&
                thisCommit.GetReferenceID(name).equals(BlobName)) {
            SA.removeAddition(name);//remove addition area
            SA.removeRemove(name);//remove removal area
            return;
        }

        /*if StagingArea have the same then do nothing,otherwise add it*/
        String GetID = SA.GetAdditionId(name);
        if (GetID != null && GetID.equals(BlobName)) {
            return;
        } else {
            SA.addAdditionHelp(name, BlobName);
            File blobToCreate = join(BLOB_DIR, BlobName);
            writeContents(blobToCreate, content);
        }
    }

    public static File returnBlob(String name, Commit thisCommit) {
        String ID = thisCommit.GetReferenceID(name);
        if (ID != null) {
            return join(BLOB_DIR, ID);
        }
        return null;
    }

}
