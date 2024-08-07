# How to work on Espresso

## Building

Set your (JVMCI-enabled) JDK via `mx` argument  e.g. `mx --java-home /path/to/java/home ...` or via `export JAVA_HOME=/path/to/java/home`. Or (easiest) run `mx fetch-jdk` to download one.

`mx build`-ing Espresso creates a GraalVM with Espresso included. You can find out where it is by running `mx graalvm-home`.

To build the default configuration (interpreter-only), on the `espresso` repository:

```bash
$ mx build
```

Other configurations are provided:

```bash
$ mx --env jvm build       # GraalVM CE + Espresso jars (interpreter only)
$ mx --env jvm-ce build    # GraalVM CE + Espresso jars (JIT)
$ mx --env native-ce build # GraalVM CE + Espresso native (JIT)

# Use the same --env argument used to build.
$ export ESPRESSO=`mx --env native-ce graalvm-home`/bin/espresso
```

Configuration files: `mx.espresso/{jvm,jvm-ce,native-ce}` and `mx.espresso/native-image.properties`

## Running Espresso

`mx espresso` runs Espresso (from jars or native) from within a GraalVM. It mimics the `java` (8|11) command. Bare `mx espresso` runs Espresso on interpreter-only mode.

```bash
$ mx --env jvm-ce build # Always build first
$ mx --env jvm-ce espresso -cp my.jar HelloWorld
```

To build and run Espresso native image:

```bash
$ mx --env native-ce build # Always build first
$ mx --env native-ce espresso -cp my.jar HelloWorld
```

The `mx espresso` launcher adds some overhead, to execute Espresso native image directly use:

```bash
$ mx --env native-ce build # Always build first
$ export ESPRESSO=`mx --env native-ce graalvm-home`/bin/espresso
$ time $ESPRESSO -cp my.jar HelloWorld
```

## Installing JARs to Maven Local

This is useful if you want to depend on your branch of Espresso as a regular Maven dependency, for example to compile it into a custom native image.

```bash
$ cd ../vm
$ mx --dy /espresso,/sulong maven-deploy --tags=public --all-suites --all-distribution-types --version-suite=sdk --suppress-javadoc
```

You can now depend on the jars using a version like `24.2.0-SNAPSHOT`.

## `mx espresso-standalone ...`

To run Espresso on a vanilla JDK (8|11) and/or not within a GraalVM use `mx espresso-standalone ...`, it mimics the `java` (8|11) command. The launcher adds all jars and properties required to run Espresso on any vanilla JDK (8|11).

To debug Espresso:

```bash
$ mx build
$ mx -d espresso-standalone -cp mxbuild/dists/jdk1.8/espresso-playground.jar com.oracle.truffle.espresso.playground.HelloWorld
```

It can also run on a GraalVM with JIT compilation:

```bash
$ mx build
$ mx --dy /compiler espresso-standalone -cp my.jar HelloWorld
```

## Dumping IGV graphs

```bash
$ mx -v --dy /compiler -J"-Djdk.graal.Dump=:4 -Djdk.graal.TraceTruffleCompilation=true -Djdk.graal.TruffleBackgroundCompilation=false" espresso-standalone -cp  mxbuild/dists/jdk1.8/espresso-playground.jar com.oracle.truffle.espresso.playground.TestMain
```

## Running Espresso cross-versions

By default, Espresso runs within a GraalVM and it reuses the jars and native libraries shipped with GraalVM. But it's possible to specify a different Java home, even with a different version; Espresso will automatically switch versions regardless of the host JVM.
```bash
$ mx build
$ mx espresso -version
$ mx espresso --java.JavaHome=/path/to/java/8/home -version
$ mx espresso --java.JavaHome=/path/to/java/11/home -version
```

## Limitations

Espresso relies on glibc's [dlmopen](https://man7.org/linux/man-pages/man3/dlopen.3.html) to run on HotSpot, but this approach has limitations that lead to crashes e.g. `libnio.so: undefined symbol: fstatat64` . Some of these limitations can be by avoided by defining `LD_DEBUG=unused` e.g.

```bash
$ LD_DEBUG=unused mx espresso -cp mxbuild/dists/jdk1.8/espresso-playground.jar com.oracle.truffle.espresso.playground.Tetris
```

## _Espressoⁿ_ Java-ception

Espresso can run itself. **Self-hosting requires a Linux distribution with an up-to-date glibc.**

Use `mx espresso-meta` to run programs on Espresso². Ensure to prepend `LD_DEBUG=unused` to overcome a known **glibc** bug.

To run HelloWorld on Espresso² execute the following:

```bash
$ mx build
$ LD_DEBUG=unused mx --dy /compiler espresso-meta -cp my.jar HelloWorld
```

It takes some time for both (nested) VMs to boot, only the base layer is blessed with JIT compilation. Enjoy!
