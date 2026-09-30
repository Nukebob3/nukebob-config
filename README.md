# NukebobConfig
[<img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/nukebobconfig)
[<img alt="youtube-singular" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/youtube-singular_vector.svg">](https://www.youtube.com/@Nukebob3)

A simple **API mod** to help create a screen for your config. (it's slightly goofy)

![A demo of the screen with two config options bouncing around](https://cdn.modrinth.com/data/nWazb6Zr/images/720c925e301d38e790aac816337465bf249e3e6a.png)

## How to implement:

### Gradle

```GRADLE
repositories {
    maven {
    name = "Modrinth"
    url = "https://api.modrinth.com/maven"
    content {
        includeGroup "maven.modrinth"
    }
}

dependencies {
    implementation "maven.modrinth:nWazb6Zr:TYmeQiXA"
}
```
You can find a specific version on the project versions page - developer info

### Java
- Create a class extending [**NukebobConfigScreen**]([NukebobConfigScreen](https://github.com/Nukebob3/nukebob-config/blob/master/src/main/java/net/nukebob/nbconfig/screen/NukebobConfigScreen.java))
- Override the setConfig method, adding your own config options with a list of [LittleNukebob](https://github.com/Nukebob3/nukebob-config/blob/master/src/main/java/net/nukebob/nbconfig/screen/LittleNukebob.java)s.
For each config option, you need to set a 
  - Text component for the hover text
  - Method reference for the config toggle
  - Method to update the visual enabled status based on the config option

See [TestConfigScreen](https://github.com/Nukebob3/nukebob-config/blob/master/src/main/java/net/nukebob/nbconfig/screen/TestConfigScreen.java) for an example implementation
``` JAVA
public class ExampleConfigScreen extends NukebobConfigScreen {
    private ExampleConfigScreen(Screen screen) {
        ...
    }
    
    @Override
    public ArrayList<LittleNukebob> setConfig() {
        return new ArrayList<>(){{
            add(new LittleNukebob(Component.literal("Show lives in nametag"), littleNukebob -> {
                MainConfig.loadConfig().showLivesInNametag=!MainConfig.loadConfig().showLivesInNametag;
            }, littleNukebob -> {
                littleNukebob.enabled = MainConfig.loadConfig().showLivesInNametag;
            }));
        }};
    }
}
```