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
public class CloudinaryUtils {
    public static final String ACTION_FILE_UPLOAD = "upload_file";
    private static Cloudinary cloudinary = null;
    private static Map<String, String> cloudinaryConfig = null;

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary();
        }
        return cloudinary;
    }

    public static Map<String, String> getCloudinaryConfig() {
        if (cloudinaryConfig == null) {
            cloudinaryConfig = new HashMap<>();
            cloudinaryConfig.put("cloud_name", <your_cloudinary_cloud_name>);
            cloudinaryConfig.put("api_key", <your_cloudinary_api_key>);
            cloudinaryConfig.put("api_secret", <your_cloudinary_api_secret>);
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
