package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.error("Must have at least one argument");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                Repository.ToCommit(args[1]);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                Repository.rm(args[1]);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Repository.Status();
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "checkout": {
                if (args.length == 3) {
                    if (args[1].equals("--")) {
                        Repository.Checkout1(args[2]);
                    }
                } else if (args.length == 4) {
                    if (args[2].equals("--")) {
                        Repository.Checkout2(args[1], args[3]);
                    }
                } else if (args.length == 2) {
                    Repository.Checkout3(args[1]);
                } else {
                    throw Utils.error("Invalid number of arguments");
                }
                break;
            }
            case "find":
                validateNumArgs("find", args, 2);
                Repository.find(args[1]);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Repository.logGlobal();
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                Repository.reset(args[1]);
            case "merge":
                validateNumArgs("merge",args,2);
                Repository.merge(args[1]);
                break;
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw Utils.error(String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}

