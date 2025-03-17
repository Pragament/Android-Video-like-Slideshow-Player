# Android-Video-like-Slideshow-Player
demo video: https://youtu.be/0Ou_gdzm8Xk

<img src="https://github.com/user-attachments/assets/74b7cf41-8291-44c4-9604-994a6d1b53c6" width="250px" />
<img src="https://github.com/user-attachments/assets/92e736da-c04f-42d2-967d-5230fa818e8a" width="250px" />
<img src="https://github.com/user-attachments/assets/e5a14508-2449-4a93-8477-60c5dd50c12b" width="250px" />
<img src="https://github.com/user-attachments/assets/1dbf9cc0-ed35-493f-acb0-56ae6c06420d" width="250px" />
<img src="https://github.com/user-attachments/assets/3c027a72-b3e0-44b2-8a85-a8f773887590" width="250px" />

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
