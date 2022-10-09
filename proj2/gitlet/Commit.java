package gitlet;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    static final File COMMIT_FOLDER = Utils.join(Repository.GITLET_DIR, "commits");
    private String message;
    /*The time when this commit is created*/
    private Date timestamp;
    private HashMap<String, String> reference = new HashMap<>();
    private String parent;

    private String SecondParent = null;

    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        if (this.parent.equals("null")) {//because sha1 using string or byte to hash, so use "null", not null
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
    }
    public Commit(String message, String parent, String secondParent){
        this.message = message;
        this.parent = parent;
        this.SecondParent = secondParent;
        if (this.parent.equals("null")) {
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
    }

    public Date GetTime() {
        return this.timestamp;
    }
    public String GetFormatTime(){
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return dateFormat.format(this.timestamp);
    }

    public String GetMassage() {
        return this.message;
    }

    public String GetParent() {
        return this.parent;
    }

    public String GetReferenceID(String Filename) {
        return reference.get(Filename);
    }

    public Set<String> GetReferenceKeySet() {
        return reference.keySet();
    }

    /*set reference from parent and StagingArea then created new commit to the file*/
    public static String SetReference(Commit toCommit, StagingArea SA) {
        Commit parentCommit = fromFile(toCommit.parent);
        toCommit.reference = new HashMap<>(parentCommit.reference);
        for (String s : SA.KetSetForAddition()) {
            toCommit.reference.put(s, SA.GetAdditionId(s));
        }
        for (String s : SA.KetSetForRemove()) {
            toCommit.reference.remove(s);
        }
        String HashCommitToReturn = toFile(toCommit);
        return HashCommitToReturn;
    }
    public  void SetReference(HashMap<String,String> hashMap){
        this.reference = hashMap;
    }

    /*read commit object from file and return*/
    public static Commit fromFile(String HashName) {
        String DirectoryName = HashName;
        if (HashName.length() > 6) {
            DirectoryName = String.copyValueOf(HashName.toCharArray(), 0, 6);
        }
        File returnDirFile = Utils.join(COMMIT_FOLDER, DirectoryName);
        if (!returnDirFile.exists()) {
            return null;
        }
        return Utils.readObject(Objects.requireNonNull(returnDirFile.listFiles())[0], Commit.class);
    }

    /*create new commit to the file and return its hashName*/
    public static String toFile(Commit newCommit) {
        String HashCommit = Commit.Sha1Commit(newCommit);
        String DirectoryName = String.copyValueOf(HashCommit.toCharArray(), 0, 6);
        /*select pre-six digits as the dirName and create Commit file to it*/
        File Direct = Utils.join(COMMIT_FOLDER, DirectoryName);
        Direct.mkdir();
        File CreatedFile = Utils.join(Direct, HashCommit);
        Utils.writeObject(CreatedFile, newCommit);
        return HashCommit;
    }
    public HashMap<String,String> GetReference(){
        return this.reference;
    }

    public static void SetCommitFile(Commit thisCommit) {
        /*delete previous file*/
        for (String fileToDelete : Objects.requireNonNull(Utils.plainFilenamesIn(Repository.CWD))) {
            Utils.restrictedDelete(fileToDelete);
        }
        /*write thisCommit file to CWD*/
        for (String FileName : thisCommit.GetReferenceKeySet()) {
            File FileToWrite = Utils.join(Repository.CWD, FileName);
            Utils.writeContents(FileToWrite,
                    (Object) Utils.readContents(
                            Objects.requireNonNull(
                                    BlobControl.returnBlob(FileName, thisCommit))));
        }
    }

    public static String Sha1Commit(Commit thisCommit) {
        return Utils.sha1(thisCommit.GetMassage(), thisCommit.GetParent(), thisCommit.GetFormatTime().toString());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Commit)) {
            return false;
        }
        Commit c = (Commit) obj;
        return this.message.equals(c.message) && this.parent.equals(c.parent) &&
                this.timestamp.toString().equals(c.timestamp.toString());
    }

    public String GetSecondParent(){return this.SecondParent;}


}
