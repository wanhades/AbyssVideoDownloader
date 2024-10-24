# AbyssVideoDownloader

**AbyssVideoDownloader** is a command-line Kotlin application that allows users to download Abyss.to videos. It supports downloading videos in different resolutions.

## Prerequisites

Before using AbyssVideoDownloader, ensure you have:

- **Java Development Kit (JDK 21)**: [Download JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

## Installation

You have two options for obtaining the `abyss-dl.jar` file:

1. **Download the Latest JAR File**:
   - You can download the latest `abyss-dl.jar` file from the [Releases](https://github.com/abdlhay/AbyssVideoDownloader/releases) section of this repository.

2. **Build the JAR File Manually**:
   - If you prefer to build the project yourself or want the latest updates, follow the instructions in the [**Building the Project**](https://github.com/abdlhay/AbyssVideoDownloader#building-the-project) section below.


## Usage

### Downloading a Video with AbyssVideoDownloader

To download a video, follow these steps:

1. **Run the Command**:
   Open your terminal and enter the following command:

   ```bash
   java -jar abyss-dl.jar [-H <header>] [--header <header>] [-o <output_file_path>] [-c <number_of_connections>]
   ```

   - **Add HTTP Headers** (Optional):
      - Use `-H <header>` or `--header <header>` to include additional information with your request.
      - Example: `-H "Authorization: Bearer TOKEN"`
      - You can add multiple headers by repeating the `-H` or `--header` option.

   - **Specify Output File** (Optional):
      - Use `-o <output_file_path>` to choose where to save the downloaded video.
      - Example: `-o /path/to/my_video.mp4`
      - If you don’t specify a path, the video will be saved in the current directory with a default name.

   - **Set Number of Connections** (Optional):
      - Use `-c <number_of_connections>` or `--connections <number_of_connections>` to set how many connections to use for downloading. This can help speed up the download.
      - The number must be between 1 and 10 by default it is set to 6.
      - Example: `-c 7`

### Examples

1. **Download a video and save it with a specific name in a chosen folder**:
   ```bash
   java -jar abyss-dl.jar -o /path/to/directory/my_video.mp4
   ```

2. **Download a video and save it with the default name in the current directory**:
   ```bash
   java -jar abyss-dl.jar
   ```

3. **Download a video using custom headers**:
   ```bash
   java -jar abyss-dl.jar -H "Authorization: Bearer TOKEN" --header "Referer: https://example.com" -o my_video.mp4
   ```

4. **Download a video with multiple connections**:
   ```bash
   java -jar abyss-dl.jar -c 7 -o my_video.mp4
   ```

5. **Download a video using custom headers and multiple connections**:
   ```bash
   java -jar abyss-dl.jar -H "Authorization: Bearer TOKEN" --header "Referer: https://example.com" -c 3 -o /path/to/my_video.mp4
   ```

6. After running the command, you will be prompted for the video URL:

   ```
   enter the video URL or ID: https://abysscdn.com/?v=K8R6OOjS7
   ```

## Building the Project

To build the AbyssVideoDownloader, you can use the Gradle Wrapper

### Steps to Build the Project

1. **Clone the Repository**:
   First, clone the project repository using Git. Open your terminal and run:

   ```bash
   git clone https://github.com/abdlhay/AbyssVideoDownloader.git
   ```

2. **Change into the Project Directory**:
   Navigate into the cloned project directory:

   ```bash
   cd AbyssVideoDownloader
   ```

3. **Run the Build Command**:
   Use the following command to build the project:

   ```bash
   ./gradlew build
   ```

   - On Windows, use the following command instead:

   ```bash
   gradlew.bat build
   ```

4. **Locate the JAR File**:
   After the build process completes successfully, the JAR file `abyss-dl.jar` will be located in the `build/libs` directory.

   - You can find the JAR file at the following path:
     ```
     build/libs/abyss-dl.jar
     ```

5. **Run the Application**:
   You can now run the application using the generated JAR file with the following command:

   ```bash
   java -jar build/libs/abyss-dl.jar
   ```

## TODOs

Here are the planned tasks and features for future updates:

- [ ] Clean Code.
- [x] Add support for multiple parallel downloads.
- [ ] Integrate dependency injection maybe
- [ ] Improve error handling and provide more descriptive messages for common issues.
- [ ] Implement retry and resume logic for failed downloads.
- [ ] Enhance logging with different verbosity levels (e.g., debug, info, error).
- [ ] Add support for proxy configuration.
- [ ] Add a graphical user interface (GUI) for easier interaction or maybe an android app

Feel free to contribute by picking any task or suggesting new ones!



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