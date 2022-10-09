package gitlet;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FileStatusCheck {
    /*The final category (“Untracked Files”) is for files present in the working directory but neither staged for addition nor tracked.
    This includes files that have been staged for removal, but then re-created without Gitlet’s knowledge.*/
    public static boolean IsUntracked(String FileName, StagingArea SA, Commit thisCommit) {
        if (!SA.KetSetForAddition().contains(FileName) &&
                !thisCommit.GetReferenceKeySet().contains(FileName)) {
            return true;
        }
        return false;
    }

    public static Set<String> UntrackedFileNameReturn(StagingArea SA, Commit thisCommit) {
        Set<String> UntrackedFileName = new HashSet<>();
        for (String fileName : Objects.requireNonNull(Utils.plainFilenamesIn(Repository.CWD))) {
            if (IsUntracked(fileName, SA, thisCommit)) {
                UntrackedFileName.add(fileName);
            }
        }
        return UntrackedFileName;
    }

    public static Set<String> ModifiedFile(StagingArea SA, Commit thisCommit) {
        Set<String> ModifiedFileName = new HashSet<>();
        for (String fileName : Objects.requireNonNull(Utils.plainFilenamesIn(Repository.CWD))) {
            /*if StagingArea have this file but with different contents */
            if (SA.KetSetForAddition().contains(fileName)) {
                if (!isSameFile(fileName, SA.GetAdditionId(fileName))) {
                    ModifiedFileName.add(fileName);
                }
            } else {   /*Staging haven't but Commit have with different contents */
                if (thisCommit.GetReferenceKeySet().contains(fileName) &&
                        !isSameFile(fileName, thisCommit.GetReferenceID(fileName))) {
                    ModifiedFileName.add(fileName);
                }
            }
        }
        return ModifiedFileName;
    }

    public static Set<String> DeleteFile(StagingArea SA, Commit thisCommit) {
        Set<String> DeleteFileName = new HashSet<>();
        /*Staged for addition, but deleted in the working directory;*/
        for (String FileName : SA.KetSetForAddition()) {
            File thisFile = Utils.join(Repository.CWD, FileName);
            if (!thisFile.exists()) {
                DeleteFileName.add(FileName);
            }
        }
        /*Not staged for removal, but tracked in the current commit
            and deleted from the working directory.*/
        for (String FileName : thisCommit.GetReferenceKeySet()) {
            File thisFile = Utils.join(Repository.CWD, FileName);
            if (!thisFile.exists() && !SA.KetSetForRemove().contains(FileName)) {
                DeleteFileName.add(FileName);
            }
        }
        return DeleteFileName;
    }

    /*check if this file's id = ID*/
    public static boolean isSameFile(String FileName, String ID) {
        File thisFile = Utils.join(Repository.CWD, FileName);
        String thisID = Utils.sha1((Object) Utils.readContents(thisFile));
        return thisID.equals(ID);
    }
}
