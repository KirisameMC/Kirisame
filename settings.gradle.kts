rootProject.name = "Kirisame"
include("minecraft-api")


if (System.getenv("JITPACK") == null) {
    include("examplePlugin")
    include("launcher")
}
include("Kirisame-Agent")