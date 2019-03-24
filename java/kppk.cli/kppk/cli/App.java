package kppk.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * App is the main entry point for Application definition and argument parsing.
 * <br/>
 * Use {@link App.AppBuilder} to create new instance of this class.
 * <p>
 * <br/><br/>
 * Example of usage:
 * <pre>{@code
 *  public class MyCli {
 *
 *      private static final StringFlag FLAG_MSG = StringFlag.builder()
 *             .setName("msg")
 *             .build();
 *
 *      public static void main(String[] args) {
 *          App.builder()
 *                 .setName("my-cli")
 *                 .setUsage("My great cli application.")
 *                 .addCommand(Command.builder()
 *                         .setName("first")
 *                         .setUsage("First command usage message")
 *                         .setArg(FLAG_MSG)
 *                         .setExecutor(ctx -> first(ctx.getArg()))
 *                         .build())
 *                 .addCommand(Command.builder()
 *                         .setName("second")
 *                         .setUsage("Second command usage message")
 *                         .setExecutor(this::second)
 *                         .build())
 *                 .addFlag(StringFlag.builder()
 *                         .setName("verbose")
 *                         .setShortName("v")
 *                         .build())
 *                 .build()
 *                 .execute(args);
 *      }
 *
 *     private void first(String msg) {
 *         System.out.println(msg);
 *     }
 *
 *     private void second(Context context) {
 *     }
 *  }
 * }</pre>
 */
public final class App {

    static final BooleanFlag FLAG_HELP = BooleanFlag.builder()
            .setName("help")
            .setShortName("h")
            .setUsage("Display this message")
            .build();

    private final String name;
    private final String usage;
    private final List<Flag> flags;
    private final List<Command> commands;
    private final HelpPrinter<App> helpPrinter;

    private App(String name, String usage, List<Flag> flags, List<Command> commands, HelpPrinter<App> helpPrinter) {
        this.name = name;
        this.usage = usage;
        this.flags = flags;
        this.commands = commands;
        this.helpPrinter = helpPrinter;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public List<Command> getCommands() {
        return commands;
    }

    /**
     * Main entry to parse program arguments.
     *
     * @param args Program arguments
     * @throws NullPointerException if args is null
     */
    public void execute(String args[]) {
        Objects.requireNonNull(args);
        Arguments arguments = new Arguments(args);
        if (arguments.isEmpty()) {
            helpPrinter.print(this, System.out);
            return;
        }
        Context.ContextBuilder contextBuilder = Context.builder().setApp(this);
        while (arguments.hasNext()) {
            String arg = arguments.next();
            if (!parseFlag(arg, arguments, contextBuilder, flags) &&
                    !parseCommand(arg, arguments, contextBuilder, flags)) {
                throw new IllegalArgumentException("Illegal argument " + arg);
            }
        }
        Context ctx = contextBuilder.build();
        // there is an help flag and no command, print app help
        if (ctx.getFlagValue(FLAG_HELP) && !ctx.hasCommand()) {
            helpPrinter.print(this, System.out);
        }

    }

    private boolean parseCommand(String arg,
                                 Arguments arguments,
                                 Context.ContextBuilder contextBuilder,
                                 List<Flag> flags) {
        Optional<Command> cmdOpt = commands.stream().filter(c -> c.matches(arg)).findFirst();
        if (cmdOpt.isPresent()) {
            Command cmd = cmdOpt.get();
            contextBuilder.setCommand(cmd);
            List<Flag> allFlags = concat(flags, cmd.getFlags());
            while (arguments.hasNext()) {
                String cmdArg = arguments.next();
                if (!parseFlag(cmdArg, arguments, contextBuilder, allFlags)) {
                    if (cmd.getArg() != null) {
                        contextBuilder.setArg(cmdArg);
                    } else {
                        throw new IllegalArgumentException("Unexpected argument " + cmdArg);
                    }
                }
            }
            cmd.execute(contextBuilder.build());
            return true;
        }
        return false;
    }

    private boolean parseFlag(String arg,
                              Arguments arguments,
                              Context.ContextBuilder contextBuilder,
                              List<Flag> flags) {
        if (Flag.isFlag(arg)) {
            Optional<Flag> flag = flags.stream().filter(f -> f.matches(arg)).findFirst();
            if (!flag.isPresent()) {
                throw new IllegalArgumentException("Unknown flag " + arg);
            }
            if (flag.get() instanceof BooleanFlag) {
                // boolean flag can be without the value
                contextBuilder.addValue(flag.get(), "true");
                return true;
            } else if (arguments.hasNext() && !Flag.isFlag(arguments.peek())) {
                // next is available and it is not flag
                contextBuilder.addValue(flag.get(), arguments.next());
                return true;
            }
            throw new IllegalArgumentException("Missing value for flag " + flag.get().getName());
        }
        return false;
    }

    private static <T> List<T> concat(List<T>... list) {
        return Stream.of(list).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Creates new instance of {@link AppBuilder}.
     *
     * @return new AppBuilder instance.
     */
    public static AppBuilder builder() {
        return new AppBuilder();
    }

    public final static class AppBuilder {
        private String name;
        private String usage;
        private List<Flag> flags = new ArrayList<>();
        private List<Command> commands = new ArrayList<>();
        private HelpPrinter<App> helpPrinter = HelpPrinter.HELP_PRINTER_APP;

        private AppBuilder() {
            // always add help flag
            flags.add(FLAG_HELP);
        }

        public AppBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public AppBuilder setUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public AppBuilder addFlag(Flag flags) {
            this.flags.add(flags);
            return this;
        }

        public AppBuilder addCommand(Command command) {
            this.commands.add(command);
            return this;
        }

        public AppBuilder setHelpPrinter(HelpPrinter<App> helpPrinter) {
            this.helpPrinter = helpPrinter;
            return this;
        }

        public App build() {
            return new App(name, usage, flags, commands, helpPrinter);
        }
    }

}
