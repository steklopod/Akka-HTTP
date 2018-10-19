name := "Akka HTTP"

version := "0.1"

scalaVersion := "2.12.6"
val akkaVersion   = "2.5.17"
val akkaHttp      = "10.1.5"
val junitJupiter  = "5.2.0"
val junitPlatform = "1.2.0"
val slf4j         = "1.8.0-beta2"
val twitter4j     = "4.0.7"

libraryDependencies ++= Seq(
  "org.slf4j"                 % "slf4j-api"               % slf4j,
  "org.slf4j"                 % "slf4j-simple"            % slf4j,
  "junit"                     % "junit"                   % "4.12" % Test,
  "org.junit.jupiter"         % "junit-jupiter-api"       % junitJupiter % Test,
  "org.junit.jupiter"         % "junit-jupiter-engine"    % junitJupiter % Test,
  "org.junit.jupiter"         % "junit-jupiter-params"    % junitJupiter % Test,
  "org.junit.platform"        % "junit-platform-launcher" % junitPlatform % Test,
  "org.junit.platform"        % "junit-platform-engine"   % junitPlatform % Test,
  "org.junit.platform"        % "junit-platform-runner"   % junitPlatform % Test,
  "com.geirsson"              %% "scalafmt-core"          % "1.5.1",
  "org.scalatest"             %% "scalatest"              % "3.2.0-SNAP10" % Test,
  "org.scalacheck"            %% "scalacheck"             % "1.13.5" % Test,
  "com.typesafe.akka"         %% "akka-actor"             % akkaVersion,
  "com.typesafe.akka"         %% "akka-slf4j"             % akkaVersion,
  "com.typesafe.akka"         %% "akka-persistence"       % akkaVersion,
  "com.typesafe.akka"         %% "akka-persistence-query" % akkaVersion,
  "com.typesafe.akka"         %% "akka-remote"            % akkaVersion,
  "com.typesafe.akka"         %% "akka-cluster"           % akkaVersion,
  "com.typesafe.akka"         %% "akka-cluster-tools"     % akkaVersion,
  "com.typesafe.akka"         %% "akka-cluster-sharding"  % akkaVersion,
  "com.typesafe.akka"         %% "akka-contrib"           % akkaVersion,
  "com.typesafe.akka"         %% "akka-stream"            % akkaVersion,
  "com.typesafe.akka"         %% "akka-testkit"           % akkaVersion % Test,
  "com.typesafe.akka"         %% "akka-stream-testkit"    % akkaVersion % Test,
  "com.typesafe.akka"         %% "akka-http"              % akkaHttp,
  "com.typesafe.akka"         %% "akka-http-spray-json"   % akkaHttp,
  "com.typesafe.akka"         %% "akka-http-testkit"      % akkaHttp % Test,
  "io.spray"                  %% "spray-json"             % "1.3.4"
)

resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("snapshots"),
  Classpaths.typesafeReleases
)
