package kppk.cli.test;

import kppk.cli.App;
import kppk.cli.Command;
import kppk.cli.Context;
import kppk.cli.StringFlag;

/**
 * {@link App} test.
 */
public class AppTest {

    public static void main(String[] args) {

        AppTest appTest = new AppTest();
//        appTest.run(new String[]{"first", "someArg"});
//        appTest.run(new String[]{"second"});
        appTest.run(new String[]{"--help", "first"});

    }

    private static final StringFlag FLAG_MSG = StringFlag.builder()
            .setName("msg")
            .build();

    private void run(String[] args) {
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
        System.out.println("---First---");
        System.out.println(msg);
    }

    private void second(Context context) {
        System.out.println("---Second---");
        System.out.println(context);
    }

}
