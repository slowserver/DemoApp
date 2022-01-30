package com.nitfol.demoapp;

/**
 * this class tries to determine the note it just heard by converting a frequency range to the letter of the note
 */
public class NoteFreq {
    public static String getNoteFromFrequency(int approxFreq) {
        if (approxFreq == 0) {
            return "NA";
        }
        while (approxFreq < 255) {
            approxFreq = approxFreq * 2;
        }
        while (approxFreq > 524) {
            approxFreq = approxFreq /2;
        }
        if (approxFreq >= 255 && approxFreq < 280) {
            return "C";
        }
        if (approxFreq >= 280 && approxFreq < 312 ) {
            return "D";
        }
        if (approxFreq >=312 && approxFreq <338 ) {
            return "E";
        }
        if (approxFreq >= 338 && approxFreq < 370) {
            return "F";
        }
        if (approxFreq >= 370 && approxFreq < 416) {
            return "G";
        }
        if (approxFreq >= 416 && approxFreq < 467) {
            return "A";
        }
        if (approxFreq >= 467 && approxFreq < 524) {
            return "B";
        }
        return "NA";
    }
    public static String getNoteFromRandom(int rand) {
        if(rand >=0 && rand < 3) {
            return "A";
        }
        if (rand >=3 && rand <6) {
            return "B";
        }
        if (rand >=6 && rand <9) {
            return "C";
        }
        if (rand >=9 && rand <12) {
            return "D";
        }
        if (rand >=12 && rand <15) {
            return "E";
        }
        if (rand >=15 && rand <19) {
            return "F";
        }
        if (rand >=19 && rand <23) {
            return "G";
        }
        return "NA";
    }//8 a 11 g //18 e 12 G 5 E 20 E 6 D  11 G 13 E 6 D 19 A 4 D 2 D
}
