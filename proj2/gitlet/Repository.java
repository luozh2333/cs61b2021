package gitlet;


import java.io.File;

import static gitlet.Utils.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static String HeadHashCode;// the sha1 of current branch's head
    public static Commit thisCommit;
    public static StagingArea SA;
    public static String CurrentBranch;//the branch name

    /*Load all the info needed,
    ex:head, staging area, before Load must needed the gitlet is init*/
    private static void LoadAll() {
        SA = StagingArea.fromFile();
        CurrentBranch = SA.GetCurrentHead();
        HeadHashCode = RefsControl.returnHead(CurrentBranch);
        thisCommit = Commit.fromFile(HeadHashCode);
    }

    /*make dir and init all the object: created first commit, let head point to it ,
       init staging area(created object and toFile )*/
    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.exit("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        Commit.COMMIT_FOLDER.mkdir();
        BlobControl.BLOB_DIR.mkdir();
        RefsControl.REFS_DIR.mkdir();
        CurrentBranch = "master";//default HEAD
        StagingArea.createdNewStagingArea(CurrentBranch);
        Commit initCommit = new Commit("initial commit", "null");
        /*created newCommit object and set head point it*/
        RefsControl.storeHead(Commit.toFile(initCommit), CurrentBranch);
    }

    /*add reference to stagingArea and create blob*/
    public static void add(String name) {
        Repository.LoadAll();
        BlobControl.addBlob(name, SA, thisCommit);
        StagingArea.toFile(SA);//update StagingArea
    }

    /*update commit from parent and StagingArea then clean up StagingArea,
     and modify head to point to this new one*/
    public static void ToCommit(String info) {
        if (info.isEmpty()) {
            Utils.exit("Please enter a commit message.");
        }
        Repository.LoadAll();
        if (SA.IsAdditionEmpty() && SA.IsRemoveEmpty()) {
            Utils.exit("No changes added to the commit.");
        }
        Commit newCommit = new Commit(info, HeadHashCode);
        HeadHashCode = Commit.SetReference(newCommit, SA);//update commit
        RefsControl.storeHead(HeadHashCode, CurrentBranch);//update head
        StagingArea.createdNewStagingArea(CurrentBranch);//clean up SA
    }

    public static void rm(String name) {
        Repository.LoadAll();
        File thisFile = join(CWD, name);
        String FileID = thisCommit.GetReferenceID(name);
        if (FileID == null && SA.GetAdditionId(name) == null) {
            Utils.exit("No reason to remove the file.");
        }
        /*Unstage the file if it is currently staged for addition*/
        SA.removeAddition(name);
        /*If the file is tracked in the current commit,
        stage it for removal and remove the file from the working directory
        if the user has not already done so*/
        if (FileID != null) {
            SA.addRemoveHelp(name, FileID);
            thisFile.delete();
        }
        StagingArea.toFile(SA);//update StagingArea
    }

    public static void branch(String BranchName) {
        Repository.LoadAll();
        File NewBranch = join(RefsControl.REFS_DIR, BranchName);
        if (NewBranch.exists()) {
            Utils.exit("A branch with that name already exists.");
        }
        RefsControl.storeHead(Commit.Sha1Commit(thisCommit), BranchName);
    }

    public static void rmBranch(String BranchName) {
        Repository.LoadAll();
        if (CurrentBranch.equals(BranchName)) {
            Utils.exit("Cannot remove the current branch.");
        }
        File Branch = join(RefsControl.REFS_DIR, BranchName);
        if (!Branch.exists()) {
            Utils.exit("branch with that name does not exist.");
        }
        Branch.delete();
    }

    public static void Checkout1(String Filename) {
        Repository.LoadAll();
        File FileCommitted = BlobControl.returnBlob(Filename, thisCommit);
        if (FileCommitted == null) {
            Utils.exit("File does not exist in that commit.");
        }
        File ToCheckout = join(CWD, Filename);
        Utils.writeContents(ToCheckout, (Object) Utils.readContents(FileCommitted));
    }

    public static void Checkout2(String CommitID, String Filename) {
        Repository.LoadAll();
        Commit theCommit = Commit.fromFile(CommitID);
        if (theCommit == null) {
            Utils.exit("No commit with that id exists.");
        }
        File FileCommitted = BlobControl.returnBlob(Filename, theCommit);
        if (FileCommitted == null) {
            Utils.exit("File does not exist in that commit.");
        }
        File ToCheckout = join(CWD, Filename);
        Utils.writeContents(ToCheckout, (Object) Utils.readContents(FileCommitted));
    }

    public static void Checkout3(String BranchName) {
        Repository.LoadAll();
        File ToBranch = join(RefsControl.REFS_DIR, BranchName);
        /*if there no this branch exist*/
        if (!ToBranch.exists()) {
            Utils.exit("No such branch exists.");
        }
        if (BranchName.equals(CurrentBranch)) {
            Utils.exit("No need to checkout the current branch.");
        }
        /*if the currentCommit have untrackedFile */
        if (!FileStatusCheck.UntrackedFileNameReturn(SA, thisCommit).isEmpty()) {
            Utils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        CurrentBranch = BranchName;
        Commit checkoutCommit = Commit.fromFile(RefsControl.returnHead(CurrentBranch));
        /*delete previous CWD file and set checkout's CommitFile to CWD*/
        Commit.SetCommitFile(checkoutCommit);
        /*clear the StagingArea */
        StagingArea.createdNewStagingArea(CurrentBranch);
    }

    public static void Status() {
        Repository.LoadAll();
        /*print Branches*/
        System.out.println("=== Branches ===");
        System.out.println("*" + CurrentBranch);
        for (String BranchName : Objects.requireNonNull(plainFilenamesIn(join(RefsControl.REFS_DIR)))) {
            if (!BranchName.equals(CurrentBranch)) {
                System.out.println(BranchName);
            }
        }
        System.out.println();
        /*print staging File*/
        System.out.println("=== Staged Files ===");
        for (String FileName : SA.KetSetForAddition()) {
            System.out.println(FileName);
        }
        System.out.println();
        /*print remove File*/
        System.out.println("=== Removed Files ===");
        for (String FileName : SA.KetSetForRemove()) {
            System.out.println(FileName);
        }
        System.out.println();
        /*print Modifications Not Staged For Commit File*/
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String FileName : FileStatusCheck.DeleteFile(SA, thisCommit)) {
            String output = FileName + " (deleted)";
            System.out.println(output);
        }
        for (String FileName : FileStatusCheck.ModifiedFile(SA, thisCommit)) {
            String output = FileName + " (modified)";
            System.out.println(output);
        }
        System.out.println();
        /*print untracked file*/
        System.out.println("=== Untracked Files ===");
        for (String FileName : FileStatusCheck.UntrackedFileNameReturn(SA, thisCommit)) {
            System.out.println(FileName);
        }
        System.out.println();
    }

    public static void reset(String CommitId) {
        Repository.LoadAll();
        String DirectoryName = String.copyValueOf(HeadHashCode.toCharArray(), 0, 6);
        if (CommitId.equals(DirectoryName) || CommitId.equals(HeadHashCode)) {
            Utils.exit("no need to reset, now in that commit");
        }
        /*if the currentCommit have untrackedFile */
        if (!FileStatusCheck.UntrackedFileNameReturn(SA, thisCommit).isEmpty()) {
            Utils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        Commit theCommit = Commit.fromFile(CommitId);
        if (theCommit == null) {
            Utils.exit("No commit with that id exists.");
        }
        /*delete previous CWD file and set checkout's CommitFile to CWD*/
        Commit.SetCommitFile(theCommit);
        /*update current branch head to this commit and clear SA*/
        RefsControl.storeHead(Commit.Sha1Commit(theCommit), CurrentBranch);//update head
        StagingArea.createdNewStagingArea(CurrentBranch);
    }

    private static String FindSplitPoint(Commit CommitTwo) {
        Comparator<Commit> commitComparator = Comparator.comparing(Commit::GetTime).reversed();
        Queue<Commit> commitsQueue = new PriorityQueue<>(commitComparator);
        commitsQueue.add(thisCommit);
        commitsQueue.add(CommitTwo);
        Set<String> checkedCommitIds = new HashSet<>();
        checkedCommitIds.add(Commit.Sha1Commit(thisCommit));
        checkedCommitIds.add(Commit.Sha1Commit(CommitTwo));
        while (!commitsQueue.isEmpty()) {
            Commit flag = commitsQueue.poll();
            String firstParent = flag.GetParent();
            String SecondParent = flag.GetSecondParent();
            if (firstParent.equals("null")) {
                return Commit.Sha1Commit(flag);//init commit
            }
            if (checkedCommitIds.contains(firstParent)) {
                return firstParent;
            } else {
                checkedCommitIds.add(firstParent);
                commitsQueue.add(Commit.fromFile(firstParent));
            }
            if (SecondParent != null) {
                if (checkedCommitIds.contains(SecondParent)) {
                    return SecondParent;
                } else {
                    checkedCommitIds.add(SecondParent);
                    commitsQueue.add(Commit.fromFile(SecondParent));
                }
            }
        }
        return null;
    }

    public static void merge(String branchTwo) {
        Commit CommitTwo = mergeHelp(branchTwo);
        Commit SplitPointCommit = Commit.fromFile(Objects.requireNonNull(FindSplitPoint(CommitTwo)));
        /*. If the split point is the same commit as the given branch,
        then we do nothing; the merge is complete, */
        assert SplitPointCommit != null;
        if (SplitPointCommit.equals(CommitTwo)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        /*If the split point is the current branch,
        then the effect is to check out the given branch*/
        if (SplitPointCommit.equals(thisCommit)) {
            CurrentBranch = branchTwo;
            Commit.SetCommitFile(CommitTwo);
            StagingArea.createdNewStagingArea(CurrentBranch);
            System.out.println("Current branch fast-forwarded.");
            return;

        }
        /*OtherWise merge two different commit*/
        String mergeInfo = "Merged " + branchTwo + " into " + CurrentBranch + ".";
        Commit newCommit = new Commit(mergeInfo, HeadHashCode, RefsControl.returnHead(branchTwo));
        newCommit.SetReference(mergeFileHelp(thisCommit, CommitTwo, SplitPointCommit));
        HeadHashCode = Commit.Sha1Commit(newCommit);
        RefsControl.storeHead(HeadHashCode, CurrentBranch);//update head
        Commit.toFile(newCommit);
        Commit.SetCommitFile(newCommit);
        StagingArea.createdNewStagingArea(CurrentBranch);//clean up SA

    }

    private static Commit mergeHelp(String branchTwo) {
        LoadAll();
        /*Cannot merge a branch with itself.*/
        if (branchTwo.equals(CurrentBranch)) {
            Utils.exit("Cannot merge a branch with itself.");
        }
        /*there are staged additions or removals present*/
        if (!SA.IsAdditionEmpty() || !SA.IsRemoveEmpty()) {
            Utils.exit("You have uncommitted changes.");
        }
        File ToBranch = join(RefsControl.REFS_DIR, branchTwo);
        /*if there no this branch exist*/
        if (!ToBranch.exists()) {
            Utils.exit("A branch with that name does not exist.");
        }
        /*There is an untracked file in the way; delete it, or add and commit it first.*/
        if (!FileStatusCheck.UntrackedFileNameReturn(SA, thisCommit).isEmpty()) {
            Utils.exit("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        return Commit.fromFile(Utils.readContentsAsString(ToBranch));
    }

    private static HashMap<String, String> mergeFileHelp(Commit CurrentCommit, Commit GivenCommit, Commit SpCommit) {
        HashMap<String, String> returnRe = new HashMap<>();
        HashMap<String, String> CurrentRe = CurrentCommit.GetReference();
        HashMap<String, String> GivenRe = GivenCommit.GetReference();
        HashMap<String, String> SpRe = SpCommit.GetReference();
        boolean IsConflict = false;
        for (String fileName : CurrentRe.keySet()) {
            //both have
            if (GivenRe.containsKey(fileName)) {//equal
                if (CurrentRe.get(fileName).equals(GivenRe.get(fileName))) {
                    returnRe.put(fileName, CurrentRe.get(fileName));
                } else if (CurrentRe.get(fileName).equals(SpRe.get(fileName))) {
                    returnRe.put(fileName, GivenRe.get(fileName));
                } else if (GivenRe.get(fileName).equals(SpRe.get(fileName))) {
                    returnRe.put(fileName, CurrentRe.get(fileName));
                } else {//not equal then concatenate
                    IsConflict = true;
                    returnRe.put(fileName, mergeTwoFile(CurrentRe.get(fileName), GivenRe.get(fileName)));
                }
            } else {//current have but Given not
                if (SpRe.containsKey(fileName)) {
                    if (!CurrentRe.get(fileName).equals(SpRe.get(fileName))) {
                        IsConflict = true;
                        returnRe.put(fileName, mergeTwoFile(CurrentRe.get(fileName), null));
                    }
                } else {//Sp haven't
                    returnRe.put(fileName, CurrentRe.get(fileName));
                }
            }
        }
        for (String fileName : GivenRe.keySet()) {
            if (!CurrentRe.containsKey(fileName)) {
                if (SpRe.containsKey(fileName)) {
                    if (!GivenRe.get(fileName).equals(SpRe.get(fileName))) {
                        IsConflict = true;
                        returnRe.put(fileName, mergeTwoFile(null, GivenRe.get(fileName)));
                    }
                } else {//Sp haven't
                    returnRe.put(fileName, GivenRe.get(fileName));
                }
            }
        }
        if (IsConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        return returnRe;
    }

    /*give two different blobs and merge them then add to blobFile*/
    private static String mergeTwoFile(String ID1, String ID2) {
        StringBuilder st = new StringBuilder();
        st.append("<<<<<<< HEAD").append("\n");
        if (ID1 != null) {
            File f1 = join(BlobControl.BLOB_DIR, ID1);
            st.append(new String(Utils.readContents(f1), StandardCharsets.UTF_8));
        }
        st.append("=======").append("\n");
        if (ID2 != null) {
            File f2 = join(BlobControl.BLOB_DIR, ID2);
            st.append(new String(Utils.readContents(f2), StandardCharsets.UTF_8));
        }
        st.append(">>>>>>>").append("\n");
        String returnID = Utils.sha1(st.toString());
        File newBlob = join(BlobControl.BLOB_DIR, returnID);
        Utils.writeContents(newBlob, st.toString());
        return returnID;
    }

    public static void log() {
        LoadAll();
        Commit flag = thisCommit;
        while (flag != null) {
            if (flag.GetSecondParent() != null) {
                System.out.println("===");
                System.out.println("commit " + Commit.Sha1Commit(flag));
                System.out.println("Merge: " +
                        String.copyValueOf(flag.GetParent().toCharArray(), 0, 7) + " "
                        + String.copyValueOf(flag.GetSecondParent().toCharArray(), 0, 7));
                System.out.println("Date: " + flag.GetFormatTime());
                System.out.println(flag.GetMassage());
                System.out.println();
            } else {
                System.out.println("===");
                System.out.println("commit " + Commit.Sha1Commit(flag));
                System.out.println("Date: " + flag.GetFormatTime());
                System.out.println(flag.GetMassage());
                System.out.println();
            }
            flag = Commit.fromFile(flag.GetParent());
        }
    }

    public static void logGlobal() {
        LoadAll();
        Commit flag;
        for (String fileName : Objects.requireNonNull(Commit.COMMIT_FOLDER.list())) {
            flag = Commit.fromFile(fileName);
            assert flag != null;
            if (flag.GetSecondParent() != null) {
                System.out.println("===");
                System.out.println("commit " + Commit.Sha1Commit(flag));
                System.out.println("Merge: " +
                        String.copyValueOf(flag.GetParent().toCharArray(), 0, 7) + " "
                        + String.copyValueOf(flag.GetSecondParent().toCharArray(), 0, 7));
                System.out.println("Date: " + flag.GetFormatTime());
                System.out.println(flag.GetMassage());
                System.out.println();
            } else {
                System.out.println("===");
                System.out.println("commit " + Commit.Sha1Commit(flag));
                System.out.println("Date: " + flag.GetFormatTime());
                System.out.println(flag.GetMassage());
                System.out.println();
            }
        }
    }

    public static void find(String CommitMassage) {
        Commit flag;
        boolean found = false;
        for (String fileName : Objects.requireNonNull(Commit.COMMIT_FOLDER.list())) {
            flag = Commit.fromFile(fileName);
            if (flag.GetMassage().equals(CommitMassage)) {
                System.out.println(Commit.Sha1Commit(flag));
                found = true;
            }
        }
        if (!found) {
            Utils.exit("Found no commit with that message.");
        }
    }
}
