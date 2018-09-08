# Classroom
A simple android app for students to keep all the important classroom stuffs at the same and secure place.

### How to use?
1. Fork and clone the repo.
2. Create a Firebase project. Enable phone auth, download `google-services.json` and paste it into `/app/` folder.
3. Create a Cloudinary project (For storage).
4. Create a Cloudinary utility file `CloudinaryUtils.java` as given below in `/utility/` folder of the project package.
5. Build and run the app.


##### CloudinaryUtils.java

```java
/**
 * A Singleton class for {@link Cloudinary} and its utilities
 */
public final class CloudinaryUtils {

    /**
     * String for post upload action
     */
    public static final String ACTION_POST_UPLOAD = "upload_post_file";

    /**
     * A static {@link Cloudinary} object. Initialized only once when first called.
     */
    private static Cloudinary cloudinary = null;

    /**
     * Configuration for our Cloudinary cloud
     */
    private static Map<String, String> cloudinaryConfig = null;

    /**
     * Returns an instance of {@link Cloudinary}.
     * If it is `null`, create an instance otherwise ust return already created one.
     *
     * @return A single instance of {@link Cloudinary}
     */
    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary();
        }
        return cloudinary;
    }

    /**
     * Gets the configuration for our Cloudinary cloud
     *
     * @return A {@link Map} having the configuration fo our Cloudinary cloud
     */
    public static Map<String, String> getCloudinaryConfig() {
        if (cloudinaryConfig == null) {
            cloudinaryConfig = new HashMap<>();
            cloudinaryConfig.put("cloud_name", <CLOUDINARY_CLOUD_NAME>);
            cloudinaryConfig.put("api_key", <CLOUDINARY_API_KEY>);
            cloudinaryConfig.put("api_secret", <CLOUDINARY_API_SECRET>);
            cloudinaryConfig.put("resource_type", "auto");
        }
        return cloudinaryConfig;
    }
}
```


### How to contribute?

1. Follow steps given in `How to use?`.
2. Update, commit and push to your repo.
3. Create a pull  request.