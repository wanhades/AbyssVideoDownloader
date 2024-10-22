# AbyssVideoDownloader

**AbyssVideoDownloader** is a command-line Kotlin application that allows users to download Abyss.to videos. It supports downloading videos in different resolutions.

## Prerequisites

Before using AbyssVideoDownloader, ensure you have:

- **Java Development Kit (JDK 21)**: [Download JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

## Installation

1. Download the latest **abyss-dl.jar** file from the [Releases](https://github.com/abdlhay/AbyssVideoDownloader/releases) section of this repository.

2. Save the jar file to a directory of your choice.

## Usage

To download a video using AbyssVideoDownloader:

1. Run the following command:

   ```bash
   java -jar abyss-dl.jar [-H <header>] [--header <header>] [-o <output_file_path>]
   ```

   - The `-H <header>` or `--header <header>` flag allows you to add HTTP headers in the format `Header-Name: Header-Value`.
   - You can specify multiple headers by repeating the `-H` or `--header` flag.
   - The `-o <output_file_path>` flag is optional. If not specified, the file will be saved in the Downloads directory with a default name.
   - The `output_file_path` can either be the file name or the full path (including the file name) where you want the video to be saved.

2. After running the command, the application will prompt you to input the video URL or the ID from Abyss.

### Examples

1. **Download a video and save it with a specified file name in a custom directory:**

   ```bash
   java -jar abyss-dl.jar -o /path/to/directory/my_video.mp4
   ```

2. **Download a video and save it with the default file name in the current directory:**

   ```bash
   java -jar abyss-dl.jar
   ```

3. **Make an HTTP request with custom headers to download a video:**

   ```bash
   java -jar abyss-dl.jar -H "Ref: Bearer TOKEN" --header "Referer: https://example.com" -o my_video.mp4
   ```

4. After running the command, you will be prompted for the video URL:

   ```
   enter the video URL or ID: https://abysscdn.com/?v=K8R6OOjS7
   ```


License
--------

    Copyright 2024 abdlhay, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.