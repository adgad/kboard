# kboard

A programmable keyboard for Android that lets you send phrases, lenny faces, macros, or data from the Internet in a single tap!

[F-Droid](https://f-droid.org/en/packages/com.adgad.kboard/) (up to date)

[Play Store](https://play.google.com/store/apps/details?id=com.adgad.kboard) (outdated)

[Demo](https://www.youtube.com/watch?v=h3i5U2tk364)

<img src="https://user-images.githubusercontent.com/1978880/51786974-b85b9780-2163-11e9-85f2-d3e64a29e0d4.jpg" alt="screenshot of kboard in action" width="400">

---


## Commands

kboard includes a VIM-like syntax for adding special macro keys, to perform tasks like cursor movements, copy/pasting and text manipulation.

A command key has the following format:

`/<friendly label>!<comma separated commands>`

Example:

`/Italicise!dw,i(_$0_)`

This creates a key titled "Italicise", which does the following: `dw` deletes the previous word and adds it to the buffer, `i` inserts the text in the brackets (which would be the deleted word wrapped in underscores.

Most commands can be preceded by a number, indicating how many times it will be run.

**Available commands:**

* `d` delete previous character
* `dw` delete previous word
* `3dw` delete previous three words
* `dd` delete all
* `dt(char)` deletes (backwards) up to and including the specified character
* `ds` - deletes either the selected text, or if nothing is selected then deletes everything.
* `yy` copy all
* `y` copy selection
* `p` paste from buffer
* `pc` paste from clipboard
* `sa` select all
* `sw` select a word, going backwards from the current cursor position
* `i(text)` output the content of the brackets ($0 gets replaced * with the last deleted/copied content)
* `iraw(text)` as i, but does not adhere to settings such as autospace or Passive Aggressive
* `upper(text)` as i, but in upper case
* `lower(text)` as i, but lower case
* `j` move cursor back by a character
* `k` move cursor forward by a character
* `b` move cursor back by a word
* `w` move cursor forward by a word
* `s` trigger a send command
* `e(...)` execute the command in the brackets
* `rnd` output a random kboard key
* `rnd(word1;word2;...)` output a random word from the comma separated list
* `rnde` output a random emoji
* `fr(from;to)` replace all instances of `from` to `to`
* `fancy(style)` outputs buffer text with funky unicode characters (circle, darkcircle,square,darksquare,double,monospace,fancy,fancybold)
* `curl(URL)` requests data from a URL and outputs it.
* `img(URL)` requests an image from a URL and enters it in compatible messaging apps (e.g. Whatsapp)
* `utf(unicode characters)` outputs the text unescaping all unicode character codes (e.g. `\u00A2`)
* `urlencode(string)` outputs the url encoded version of a string (e.g. `hello%3aworld`)
* `qq` switch to the keyboard application you have been using before (same as pressing the earth-wireframe key) after the rest of the commands has been executed.
* `undo` undo the last change (limited to changes made by kboard)

**Examples of use**

`/Italicise Previous!dw,i($0)`

`/Bolden Previous!dw,i($0)`

`/Italics!i(__),j` - this writes the two underscores then moves the cursor between them

`/Copy All!yy`

`/Repeat word x4!dw,4p` - repeats the previous word 4 times

`/rly sad!1000i(😥)`

`/darkcircle!ds,fancy(darkcircle)` - replaces selection or everything with 🅣🅔🅧🅣 🅛🅘🅚🅔 🅣🅗🅘🅢. Choose from `circle`, `darkcircle`, `square`,`darksquare`,`double`,`monospace`,`fancy`,`fancybold`.

_Warning_: weird unicode characters look cool but break accessibility software, so please don't use in public places!

`/exec!dt(!),e($0)` - this is meta, it enables you to write and execute commands inline in a text field by writing e.g. `!10e(^, i(* ), ^)` and hitting the exec key

`/bullets!10e(^, i(* ), ^)` - prepends "* " the the previous 10 lines

`/emojigame!10rnde` - outputs 10 random emoji

`/feeling lucky!rnd,s` - sends a random kboard key

`/birthday!dw,rnd(Happy Birthday name;Hey name, happy birthday.;hb2u;Have a great day name),fr(name,$0)` - write a name, then press this key to send a random birthday message to this name

`/bdaysend!birthday,s` - Can also have commands that refer to other command keys you've made'

`/uuid!curl(https://httpbin.org/uuid)` - Can make arbitrary GET requests to URLS

---

## CURL examples

The `curl` commands allows you to create your own HTTP endpoints to output anything you like!

Here are some examples of how it can be used:

`/yt!ds,curl(https://kboard-api.glitch.me/youtube/$0)`
An endpoint that searches the Youtube API, and returns the URL for the first video found.

`/wolfram!ds,curl(https://api.wolframalpha.com/v1/result?appid=<APP_ID>&i=$0)`
Uses the [Wolfram Short Answers API](https://products.wolframalpha.com/short-answers-api/documentation/) to retrieve answers to anything [Wolfram Alpha](https://www.wolframalpha.com/) can answer. (Requires API key)

`/cat!curl(https://kboard-api.glitch.me/catfact)`
Outputs a cat fact from a [Cat Fact API](https://alexwohlbruck.github.io/cat-facts/)

`/dad joke!curl(https://icanhazdadjoke.com/)`
Outputs a random lame joke.

## Accessibility Service (optional)

kboard provides an accessibility service which can be used to populate a person's name and messages from Whatsapp conversations.

This can be accessed with the keywords `$name`, `$fname` and `$lname`, and `$wachat`.

Getting the right content with the accessibility service is a bit flaky, generally going home/back will pick up the right content from a Whatsapp Conversation.

This service is optional, and the keyboard is fully functional without it. 

Any content retrieved from WhatsApp is not sent or logged anywhere unless you explicitly do so yourself (e.g. by using it alongside `curl`)

