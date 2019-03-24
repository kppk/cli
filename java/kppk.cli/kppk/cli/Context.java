package kppk.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Context is used to store the result of parsing.
 */
public final class Context {

    private final App app;
    private final Command command;
    private final Map<Flag, String> values;
    private final String arg;

    public String getArg() {
        return arg;
    }

    public <T> T getFlagValue(Flag<T> flag) {
        String valStr = values.get(flag);
        if (valStr != null) {
            return flag.convert(valStr);
        }
        return flag.getDefaultValue();
    }

    public Map<String, String> getFlagValues() {
        return values.entrySet().stream()
                .map(entry -> new HashMap.SimpleEntry<>(entry.getKey().getName(), entry.getValue()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    boolean hasCommand() {
        return command != null;
    }

    private Context(App app, Command command, String arg, Map<Flag, String> values) {
        this.app = app;
        this.command = command;
        this.arg = arg;
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Context{");
        sb.append("app=").append(app);
        sb.append(", command=").append(command);
        sb.append(", values=").append(values);
        sb.append(", arg='").append(arg).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public final static class ContextBuilder {

        private App app;
        private Map<Flag, String> values = new HashMap<>();
        private Command command;
        private String arg;

        private ContextBuilder() {
        }

        public ContextBuilder setApp(App app) {
            this.app = app;
            return this;
        }

        public ContextBuilder setCommand(Command command) {
            this.command = command;
            return this;
        }

        public ContextBuilder setArg(String value) {
            this.arg = value;
            return this;
        }

        public ContextBuilder addValue(Flag flag, String value) {
            values.put(flag, value);
            return this;
        }

        public Context build() {
            return new Context(app, command, arg, values);
        }


    }
}
