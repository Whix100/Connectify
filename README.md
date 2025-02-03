# **THIS IS A WIP FORK INTENDED TO BE A STANDALONE MOD**

[![Build master](https://img.shields.io/github/actions/workflow/status/Whix100/Connectify/build.yml?style=flat-square&branch=master)](https://github.com/Whix100/Connectify/actions?query=workflow%3A%22Build+master%22)
[![Latest Release](https://img.shields.io/github/v/release/Whix100/Connectify?style=flat-square&label=Release)](https://github.com/Whix100/Connectify/releases)
[![Latest PreRelease](https://img.shields.io/github/v/release/Whix100/Connectify?include_prereleases&style=flat-square&label=Pre)](https://github.com/Whix100/Connectify/releases)

# Connectify

## Table of Contents

* [About](#about)
* [Contacts](#contacts)
* [License](#license)
* [Downloads](#downloads)
* [Installation](#installation)
* [Issues](#issues)
* [API](#applied-energistics-2-api)
* [Building](#building)
* [Contribution](#contribution)
* [Credits](#credits)

## About

A Minecraft mod for automating storage and processing through computers

## Contacts

* [GitHub](https://github.com/Whix100/Connectify)

## License

* Connectify
  - (c) 2013 - 2025 Whix100
  - [![License](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://raw.githubusercontent.com/Whix100/Connectify/rv2/LICENSE)
* Applied Energistics 2 API
  - (c) 2013 - 2020 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-MIT-red.svg?style=flat-square)](http://opensource.org/licenses/MIT)
* Applied Energistics 2
  - (c) 2013 - 2020 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat-square)](https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv2/LICENSE)
* AE2 Textures and Models
  - (c) 2020, [Ridanisaurus Rid](https://github.com/Ridanisaurus/), (c) 2013 - 2020 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%203.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
* AE2 Text and Translations
  - [![License](https://img.shields.io/badge/License-No%20Restriction-green.svg?style=flat-square)](https://creativecommons.org/publicdomain/zero/1.0/)
* Additional Sound Licenses
  - Guidebook Click Sound
    - [EminYILDIRIM](https://freesound.org/people/EminYILDIRIM/sounds/536108/) 
    - [![License](https://img.shields.io/badge/License-CC%20BY%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by/4.0/)

## Downloads

*Not available yet*

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It has no additional hard dependencies.

## Issues

Connectify crashing, have a suggestion, found a bug?  Create an issue now!

1. Make sure your issue has not already been answered or fixed and you are using the latest version. Also think about whether your issue is a valid one before submitting it.
    * If it is already possible with vanilla and AE2 itself, the suggestion will be considered invalid.
    * Asking for a smaller version, more compact version, or more efficient version of something will also be considered invalid.
2. Go to [the issues page](https://github.com/Whix100/Connectify/issues) and click [new issue](https://github.com/Whix100/Connectify/issues/new)
3. If applicable, use one of the provided templates. It will also contain further details about required or useful information to add.
4. Click `Submit New Issue`, and wait for feedback!

Providing as many details as possible does help us to find and resolve the issue faster and also you getting a fixed version as fast as possible.

Please note that we might close any issue not matching these requirements. 

## Applied Energistics 2 API

The API for Applied Energistics 2. It is open source to discuss changes, improve documentation, and provide better add-on support in general. [See more.](https://github.com/AppliedEnergistics/Applied-Energistics-2)

## Building

1. Clone this repository via 
  - SSH `git clone git@github.com:Whix100/Connectify.git` or 
  - HTTPS `git clone https://github.com/Whix100/Connectify.git`
2. Build using the `gradlew runData build` command. Jar will be in `build/libs`
3. For core developer: Load the Gradle project in your IDE

## Contribution

Before you want to add major changes, you might want to discuss them with us first, before wasting your time.
If you are still willing to contribute to this project, you can contribute via [Pull-Request](https://help.github.com/articles/creating-a-pull-request).

The [guidelines for contributing](https://github.com/Whix100/Connectify/blob/master/.github/CONTRIBUTING.md) contain more detailed information about topics like the used code style and should also be considered.

Here are a few things to keep in mind that will help get your PR approved.

* A PR should be focused on content. Any PRs where the changes are only syntax will be rejected.
* Use the file you are editing as a style guide.
* Consider your feature.
  - Is your suggestion already possible using Vanilla + AE2?
  - Make sure your feature isn't already in the works, or hasn't been rejected previously.
  - Does your feature simplify another feature of AE2? These changes will not be accepted.
  - If your feature can be done by any popular mod, discuss with us first.

**Getting Started**

1. Fork this repository
2. Clone the fork via
  * SSH `git clone git@github.com:<your username>/Connectify.git` or 
  * HTTPS `git clone https://github.com/<your username>/Connectify.git`
3. Change code base
4. Run `gradlew spotlessApply` to apply automatic code formatting
5. Add changes to git `git add -A`
6. Commit changes to your clone `git commit -m "<summary of made changes>"`
7. Push to your fork `git push`
8. Create a Pull-Request on GitHub
9. Wait for review
10. Squash commits for cleaner history

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork. Also read up about [synching](https://help.github.com/articles/syncing-a-fork) if you plan to contribute on regular basis.

### Encoding

Files must be encoded as UTF-8.

## Credits

Thanks to all of our [contributors](https://github.com/Whix100/Connectify/graphs/contributors) and the [contributors](https://github.com/AppliedEnergistics/Applied-Energistics-2/graphs/contributors) of Applied Energistics 2!
