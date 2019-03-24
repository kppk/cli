package kppk.cli;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface to implement to provide help printers.
 */
@FunctionalInterface
public interface HelpPrinter<T> {

    void print(T target, PrintStream out);

    HelpPrinter<App> HELP_PRINTER_APP = (app, out) -> {
        Formatter formatter = new Formatter(out);
        formatter.format("%s\n", app.getUsage());
        formatter.format("\n");
        formatter.format("Usage:\n");
        formatter.format("\t%s [options]\n", app.getName());
        formatter.format("\n");
        formatter.format("Options:\n");
        app.getFlags().forEach(f -> {
            formatter.format("\t%-20s%s\n", concat(prefix("-", f.getShortName()), prefix("--", f.getName())), f.getUsage());
        });
        formatter.format("\n");
        formatter.format("Commands:\n");
        app.getCommands().forEach(c -> {
            formatter.format("\t%-20s%s\n", concat(c.getShortName(), c.getName()), c.getUsage());
        });
    };

    HelpPrinter<Command> HELP_PRINTER_CMD = (cmd, out) -> {
        boolean hasFlags = !cmd.getFlags().isEmpty();
        Formatter formatter = new Formatter(out);
        formatter.format("Name:\n");
        formatter.format("\t%s - %s\n", cmd.getName(), cmd.getUsage());
        formatter.format("Usage:\n");
        if (cmd.getFlags().isEmpty()) {
            formatter.format("\t%s [arguments...]\n", cmd.getName());
        } else {
            formatter.format("\t%s [command options] [arguments...]\n", cmd.getName());
            formatter.format("Options:\n");
            cmd.getFlags().forEach(f -> {
                formatter.format("\t%-20s%s\n", concat(prefix("-", f.getShortName()), prefix("--", f.getName())), f.getUsage());
            });
        }
    };

    static String concat(String... str) {
        return Stream.of(str).filter(Objects::nonNull).collect(Collectors.joining(","));
    }

    static String prefix(String prefix, String s) {
        if (s != null) {
            return prefix + s;
        }
        return null;
    }
}
