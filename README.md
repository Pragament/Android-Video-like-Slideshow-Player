# Android-Video-like-Slideshow-Player

Current API: https://pastebin.com/raw/EEH7viWc

```
public interface SlideshowGetDataService {
  @GET("raw/EEH7viWc")
  Call<SlideshowJsonModel> getAllJson();
}
```

```
public class SlideshowRetrofitInstance {
    private static final String BASE_URL = "https://pastebin.com/";
}
