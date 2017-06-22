///<reference path="../CommonTypes.ts"/>
///<reference path="./Util.ts"/>

/* tslint:disable:no-bitwise*/

/**
 * Pseudo Random Number Generator implementation.
 * Adapted from David Bau under MIT LICENSE. https://github.com/davidbau/seedrandom
 * 
 * Original License note:
 * 
 * Copyright (C) 2010 by Johannes Baagøe <baagoe@baagoe.org>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
namespace Util {
    export namespace PRNG {
        // holds the prng state
        const me = {
            s0: 0,
            s1: 0,
            s2: 0,
            c: 0,
        };

        export function setSeed(seed: string) {

            let n = 0xefc8249d;
            function mash(data: string) {
                data = data.toString();
                for (let i = 0; i < data.length; i++) {
                    n += data.charCodeAt(i);
                    let h = 0.02519603282416938 * n;
                    n = h >>> 0;
                    h -= n;
                    h *= n;
                    n = h >>> 0;
                    h -= n;
                    n += h * 0x100000000; // 2^32
                }
                return (n >>> 0) * 2.3283064365386963e-10; // 2^-32
            }

            me.c = 1;
            me.s0 = mash(" ");
            me.s1 = mash(" ");
            me.s2 = mash(" ");
            me.s0 -= mash(seed);
            if (me.s0 < 0) {
                me.s0 += 1;
            }
            me.s1 -= mash(seed);
            if (me.s1 < 0) {
                me.s1 += 1;
            }
            me.s2 -= mash(seed);
            if (me.s2 < 0) {
                me.s2 += 1;
            }
        }

        export function nextIdNumber(): IdNumber {
            const id: number = (nextUint32() + ((nextUint32() & 0x1FFFFF) * 0x100000000));
            return id.toString(16);
        }

        export function nextUint32(): number {
            return (next() * 0x100000000) >>> 0;
        }

        function next() {
            const t = 2091639 * me.s0 + me.c * 2.3283064365386963e-10; // 2^-32
            me.s0 = me.s1;
            me.s1 = me.s2;
            return me.s2 = t - (me.c = t | 0);
        }

        function defaultSeed(): string {
            // use strong seed if available, should be there in most cases
            // according to "caniuse.com"
            const crypto: Crypto = window.crypto || (window as any).msCrypto;
            if (crypto) {
                const rndValues = new Uint8Array(64);
                crypto.getRandomValues(rndValues);
                return String.fromCharCode.apply(0, rndValues);
            } else {
                // alternative seed
                return Util.timestampMS().toString() + window.navigator.userAgent;
            }

        }

        setSeed(defaultSeed());
    }
}