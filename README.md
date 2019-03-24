cli
===

Very simple, 0 dependencies, no reflection CLI command parsing library in Java, which can be easily used with applications compiled by GRAAL's SubtrateVM to native binaries.

Usage example:

``` java
public class MyCli {
    
    private static final StringFlag FLAG_MSG = StringFlag.builder()
                .setName("msg")
                .build();
    
    public static void main(String[] args) {
        App.builder()
            .setName("my-cli")
            .setUsage("My great cli application.")
            .addCommand(Command.builder()
                    .setName("first")
                    .setUsage("First command usage message")
                    .setArg(FLAG_MSG)
                    .setExecutor(ctx -> first(ctx.getArg()))
                    .build())
            .addCommand(Command.builder()
                    .setName("second")
                    .setUsage("Second command usage message")
                    .setExecutor(this::second)
                    .build())
            .addFlag(StringFlag.builder()
                    .setName("verbose")
                    .setShortName("v")
                    .build())
            .build()
            .execute(args);
    }
    
    private void first(String msg) {        
        System.out.println(msg);
    }

    private void second(Context context) {        
        System.out.println(context);
    }
} 
```