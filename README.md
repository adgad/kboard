# kboard
Android keyboard for fast responses

[View in Play Store](https://play.google.com/store/apps/details?id=com.adgad.kboard)

====

###Description

Do you hate people as much as I do?
Do you find yourself wasting time trying to find the right condescending message in order to get rid of them?

Then you need kboard - a keyboard designed specifically to end conversations. Don't waste time with all those other superfluous letters when all you really need is 'k.'.


* Add as many custom keys as you want
* Quickly switch between kboard and your other keyboards
* One-click send option for the pinnacle of laziness
* COMMANDS - a powerful, VIM-like syntax for custom key commands and macros (more below).

Designed to save you hours of pointless talking!

====

### Commands

Commands is a powerful (I hope) VIM-like way of adding macro keys to the kboard for things like cursor movements, copying/pasting and text manipulation.

I actually only wanted an easy way to make words bold/italic on Whatsapp.
Now, with commands, I can add a key like this:

`/Italicise!dw,i(_$0_)`

Which will yank the previous word then output it wrapped in underscores. LIke magic.

A command key has the following format:

`/<friendly label>!<comma separated commands>`

Most commands can be preceded by a number, indicating how many times it will be run.

Some commands take parameters in parentheses. The full list of available commands are:


* `d` delete previous character
* `dw` delete previous word
* `3dw` delete previous three words
* `dd` delete all
* `dt(char)` - deletes (backwards) up to and including the specified character
* `yy` copy all
* `y` copy selection
* `p` paste from clipboard
* `i(text)` output the content of the brackets ($0 gets replaced * with the last deleted/copied content)
* `upper(text)` as i, but in upper case
* `lower(text)` as i, but lower case
* `j` move cursor back by a character
* `k` move cursor forward by a character
* `b` move cursor back by a word
* `w` move cursor forward by a word
* `s` trigger a send command
* `e(...)` execute the command in the brackets
* `rnd` output a random kboard key
* `rnde` output a random emoji

**Examples of use**

`/Italicise Previous!dw,i($0)`

`/Bolden Previous!dw,i($0)`

`/Italics!i(__),j` - this writes the two underscores then moves the cursor between them

`/Copy All!yy`

`/Repeat word x4!dw,4p` - repeats the previous word 4 times

`/rly sad!1000i(😥)`

`/exec!dt(!),e($0)` - this is quite a powerful key to have, it enables you to write and execute commands inline in a text field by writing e.g. `!10e(^, i(* ), ^)` and hitting the exec key

`/bullets!10e(^, i(* ), ^)` - prepends "* " the the previous 10 lines

`/emojigame!10rnde` - outputs 10 random emoji

`/feeling lucky!rnd,s` - sends a random kboard key

====

###Other use cases of kboard

* Add your favourite hashtags
* Use to speed up github peer reviews (e.g. thumbs up, +1, LGTM)
* Add long names that are awkward to type
* Custom text emoji/faces (e.g. ¯\_(ツ)_/¯, ( ͡° ͜ʖ ͡°))
* Automate sweet nothings to your partner


