package com.github.dreamhead.moco.runner;

import com.github.dreamhead.moco.bootstrap.arg.StartArgs;
import com.github.dreamhead.moco.runner.watcher.RunnerWatcher;
import com.github.dreamhead.moco.runner.watcher.MonitorFactory;
import com.google.common.base.Optional;

import java.io.File;

import static com.github.dreamhead.moco.runner.FileRunner.createConfigurationFileRunner;
import static com.github.dreamhead.moco.runner.FileRunner.createSettingFileRunner;

public class RunnerFactory {
    private final MonitorFactory factory = new MonitorFactory();
    private final String shutdownKey;

    public RunnerFactory(final String shutdownKey) {
        this.shutdownKey = shutdownKey;
    }

    public ShutdownRunner createRunner(final StartArgs startArgs) {
        Runner dynamicRunner = createDynamicRunner(startArgs);
        return createShutdownRunner(dynamicRunner, startArgs.getShutdownPort(), shutdownKey);
    }

    public ShutdownRunner createShutdownRunner(final Runner runner, final Optional<Integer> shutdownPort,
                                               final String shutdownKey) {
        return new ShutdownRunner(runner, factory.createShutdownWatcher(runner, shutdownPort, shutdownKey));
    }

    private Runner createDynamicRunner(final StartArgs startArgs) {
        if (startArgs.hasConfigurationFile()) {
            return createDynamicConfigurationRunner(startArgs);
        }

        return createDynamicSettingRunner(startArgs);
    }

    private Runner createDynamicSettingRunner(final StartArgs startArgs) {
        final File settingsFile = new File(startArgs.getSettings().get());
        final FileRunner fileRunner = createSettingFileRunner(settingsFile, startArgs);
        final SettingRunner runner = (SettingRunner) fileRunner.getRunner();
        RunnerWatcher watcher = factory.createSettingWatcher(settingsFile,
                runner.getFiles(), fileRunner);
        return new MonitorRunner(fileRunner, watcher);
    }

    private Runner createDynamicConfigurationRunner(final StartArgs startArgs) {
        final File configuration = new File(startArgs.getConfigurationFile().get());
        final FileRunner fileRunner = createConfigurationFileRunner(configuration, startArgs);
        RunnerWatcher watcher = factory.createConfigurationWatcher(configuration, fileRunner);
        return new MonitorRunner(fileRunner, watcher);
    }
}
