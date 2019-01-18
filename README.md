# kboard

A programmable keyboard for Android that lets you send phrases, lenny faces, macros, or data from the Internet in a single tap!

[View in Play Store](https://play.google.com/store/apps/details?id=com.adgad.kboard)

[Demo](https://www.youtube.com/watch?v=h3i5U2tk364)

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
* `y` select all
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
Uses the [Wolfram Short Answers API](https://products.wolframalpha.com/short-answers-api/documentation/) to retrieve answers to anything [Wolfram Alpha](https://www.wolframalpha.com/) can answer.

`/cat!curl(https://kboard-api.glitch.me/catfact)`
Outputs a cat fact from a [Cat Fact API](https://alexwohlbruck.github.io/cat-facts/)

`/dad joke!curl(https://icanhazdadjoke.com/api)`
Outputs a random lame joke.


