package com.googlecode.cryptogwt.provider;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

public final class JsCryptoNativeAES extends JavaScriptObject {

    static JsCryptoNativeAES newInstance(byte[] key) { 
        return nativeNewInstance(JsArrayUtils.toJsArrayInteger(key)); 
    }
    
    private static native JsCryptoNativeAES nativeNewInstance(JsArrayInteger key) /*-{
    // aes object constructor. Takes as arguments:
    //- 16-byte key, or an array of 4 32-bit words
    //
    function aes(key) {        
        this._decryptScheduled = false;
        
        // AES round constants
        this._RCON = [
            [0x00, 0x00, 0x00, 0x00],
            [0x01, 0x00, 0x00, 0x00],
            [0x02, 0x00, 0x00, 0x00],
            [0x04, 0x00, 0x00, 0x00],
            [0x08, 0x00, 0x00, 0x00],
            [0x10, 0x00, 0x00, 0x00],
            [0x20, 0x00, 0x00, 0x00],
            [0x40, 0x00, 0x00, 0x00],
            [0x80, 0x00, 0x00, 0x00],
            [0x1b, 0x00, 0x00, 0x00],
            [0x36, 0x00, 0x00, 0x00]
        ];
    
        if ((key.length == 4) || (key.length == 8)) {
            this._key = [];
            aes.wordsToBytes(key, this._key);
        }
        else
            this._key = key;
        
        this._nr = 6 + this._key.length/4;
        
        // initialize tables that will be precomputed
        this._SBOX = [];
        this._INV_SBOX = [];
        this._T = new Array(4);
        this._Tin = new Array(4);
        for (var i=0; i < 4; i++) {
            this._T[i] = [];
            this._Tin[i] = [];
        }
        
        this._precompute(); 
        this.scheduleEncrypt();
    
        // initialize encryption and decryption buffers
        this._ctBuffer = [];
        this._ptBuffer = [];
    }
        
    
    //////////////////
    // KEY SCHEDULING
    //////////////////
    
    aes.prototype.scheduleEncrypt = function () {
        this._decryptScheduled = false;
        this._w = [];
        var key = [];
        if ((this._key.length == 16) || (this._key.length == 32)) aes.bytesToWords(this._key, key);
        else key = this._key;
        var klen = key.length;
        var j = 0;
    
        var w = [];
        var s = this._SBOX;
        for (var i=0; i < klen; i++) w[i] = key[i];
        
        for (var i=klen; i < 4*(this._nr+1); i++) {
            var temp = w[i-1];
            if (i % klen == 0) {
                temp = s[temp >>> 16 & 0xff] << 24 ^
                s[temp >>> 8 & 0xff] << 16 ^
                s[temp & 0xff] << 8 ^
                s[temp >>> 24] ^ this._RCON[j+1][0] << 24;
                j++;
            } else if (klen == 8 && i % klen == 4) {
                temp = s[temp >>> 24] << 24 ^ s[temp >>> 16 & 0xff] << 16 ^ s[temp >>> 8 & 0xff] << 8 ^ s[temp & 0xff];
            }
            w[i] = w[i-klen] ^ temp;
        }
    
        var wlen = w.length/4;
        for (var i=0; i < wlen; i++) {
            var j = i * 4;
            this._w[i] = [];
            this._w[i][0] = w[j];
            this._w[i][1] = w[j+1];
            this._w[i][2] = w[j+2];
            this._w[i][3] = w[j+3];
        }
        
    };
    
    aes.prototype.scheduleDecrypt = function() {
        if (!this._w) this.scheduleEncrypt();
        if (this._decryptScheduled) return;
        this._decryptScheduled = true;
            
        var temp = [];
        var j = this._w.length-1;
        for (var i=0; i<j; i++) {
            temp[0] = this._w[i][0];
            temp[1] = this._w[i][1];
            temp[2] = this._w[i][2];
            temp[3] = this._w[i][3];
            this._w[i][0] = this._w[j][0];
            this._w[i][1] = this._w[j][1];
            this._w[i][2] = this._w[j][2];
            this._w[i][3] = this._w[j][3];
            this._w[j][0] = temp[0];
            this._w[j][1] = temp[1];
            this._w[j][2] = temp[2];
            this._w[j][3] = temp[3];
            j--;
        }
    
        var td0 = this._Tin[0], td1 = this._Tin[1], td2 = this._Tin[2], td3 = this._Tin[3], te1 = this._T[1];
        for (var i=1; i < this._w.length-1; i++) {
            this._w[i][0] = td0[te1[(this._w[i][0] >>> 24)       ] & 0xff] ^
                td1[te1[(this._w[i][0] >>> 16) & 0xff] & 0xff] ^
                td2[te1[(this._w[i][0] >>>  8) & 0xff] & 0xff] ^
                td3[te1[(this._w[i][0]      ) & 0xff] & 0xff];
            this._w[i][1] = td0[te1[(this._w[i][1] >>> 24)       ] & 0xff] ^
                td1[te1[(this._w[i][1] >>> 16) & 0xff] & 0xff] ^
                td2[te1[(this._w[i][1] >>>  8) & 0xff] & 0xff] ^
                td3[te1[(this._w[i][1]      ) & 0xff] & 0xff];
            this._w[i][2] = td0[te1[(this._w[i][2] >>> 24)       ] & 0xff] ^
                td1[te1[(this._w[i][2] >>> 16) & 0xff] & 0xff] ^
                td2[te1[(this._w[i][2] >>>  8) & 0xff] & 0xff] ^
                td3[te1[(this._w[i][2]      ) & 0xff] & 0xff];
            this._w[i][3] = td0[te1[(this._w[i][3] >>> 24)       ] & 0xff] ^
                td1[te1[(this._w[i][3] >>> 16) & 0xff] & 0xff] ^
                td2[te1[(this._w[i][3] >>>  8) & 0xff] & 0xff] ^
                td3[te1[(this._w[i][3]      ) & 0xff] & 0xff];
        }
        
    };
    
    
    /////////////////////////
    // ENCRYPTION/DECRYPTION
    /////////////////////////
    
    
    //  Encrypts a single block message in AES. Takes the plaintext, an array in which to dump
    //  the ciphertext, and a boolean decrypt argument. If set to true, this function acts as
    //  a decryption function.
    //  block and ciphertext are both arrays of 4 32-bit words.
    //    
    aes.prototype.encryptBlock = function(block, ciphertext, decrypt) {
        if (block.length != 4) return;
        if (!decrypt && this._decryptScheduled) this.scheduleEncrypt();
    
        // get key schedule
        var w = this._w;
        // load round transformation tables
        var te0, te1, te2, te3;
        if (decrypt) {
            te0 = this._Tin[0];
            te1 = this._Tin[1];
            te2 = this._Tin[2];
            te3 = this._Tin[3];
        } else {
            te0 = this._T[0];
            te1 = this._T[1];
            te2 = this._T[2];
            te3 = this._T[3];
        }
        
        // perform rounds
        var rk = w[0];
        var s0 = block[0] ^ rk[0];
        var s1 = block[1] ^ rk[1];
        var s2 = block[2] ^ rk[2];
        var s3 = block[3] ^ rk[3];
        var t0,t1,t2,t3;
        rk = w[1];
        var order = [];
        var nr = w.length-1;
        for (var round = 1; round < nr; round++) {
            order = [s1, s2, s3, s0];
            if (decrypt) order = [s3, s0, s1, s2];
            t0 = te0[(s0>>>24)] ^ te1[(order[0]>>>16) & 0xff]^ te2[(s2>>>8)&0xff] ^ te3[order[2]&0xff] ^ rk[0];
            t1 = te0[(s1>>>24)] ^ te1[(order[1]>>>16) & 0xff]^ te2[(s3>>>8)&0xff] ^ te3[order[3]&0xff] ^ rk[1];
            t2 = te0[(s2>>>24)] ^ te1[(order[2]>>>16) & 0xff]^ te2[(s0>>>8)&0xff] ^ te3[order[0]&0xff] ^ rk[2];
            t3 = te0[(s3>>>24)] ^ te1[(order[3]>>>16) & 0xff]^ te2[(s1>>>8)&0xff] ^ te3[order[1]&0xff] ^ rk[3];
            s0 = t0;
            s1 = t1;
            s2 = t2;
            s3 = t3;
            rk = w[round+1];
        }
        if (decrypt) {
            s0 = ((this._INV_SBOX[(t0>>>24)])<<24) ^ ((this._INV_SBOX[(t3>>>16)&0xff])<<16) ^ ((this._INV_SBOX[(t2>>>8)&0xff])<<8) ^ (this._INV_SBOX[(t1)&0xff]) ^ rk[0];
            s1 = ((this._INV_SBOX[(t1>>>24)])<<24) ^ ((this._INV_SBOX[(t0>>>16)&0xff])<<16) ^ ((this._INV_SBOX[(t3>>>8)&0xff])<<8) ^ (this._INV_SBOX[(t2)&0xff]) ^ rk[1]
            s2 = ((this._INV_SBOX[(t2>>>24)])<<24) ^ ((this._INV_SBOX[(t1>>>16)&0xff])<<16) ^ ((this._INV_SBOX[(t0>>>8)&0xff])<<8) ^ (this._INV_SBOX[(t3)&0xff]) ^ rk[2];
            s3 = (this._INV_SBOX[(t3>>>24)]<<24) ^ (this._INV_SBOX[(t2>>>16)&0xff]<<16) ^ (this._INV_SBOX[(t1>>>8)&0xff]<<8) ^ (this._INV_SBOX[(t0)&0xff]) ^ rk[3];
        } else {
            s0 = (te2[t0>>>24]&0xff000000) ^ (te3[(t1>>>16)&0xff]&0x00ff0000) ^ (te0[(t2>>>8)&0xff]&0x0000ff00) ^ (te1[(t3)&0xff]&0x000000ff) ^ rk[0];
            s1 = (te2[t1>>>24]&0xff000000) ^ (te3[(t2>>>16)&0xff]&0x00ff0000) ^ (te0[(t3>>>8)&0xff]&0x0000ff00) ^ (te1[(t0)&0xff]&0x000000ff) ^ rk[1];
            s2 = (te2[t2>>>24]&0xff000000) ^ (te3[(t3>>>16)&0xff]&0x00ff0000) ^ (te0[(t0>>>8)&0xff]&0x0000ff00) ^ (te1[(t1)&0xff]&0x000000ff) ^ rk[2];
            s3 = (te2[t3>>>24]&0xff000000) ^ (te3[(t0>>>16)&0xff]&0x00ff0000) ^ (te0[(t1>>>8)&0xff]&0x0000ff00) ^ (te1[(t2)&0xff]&0x000000ff) ^ rk[3];
        }
        ciphertext[0] = s0;
        ciphertext[1] = s1;
        ciphertext[2] = s2;
        ciphertext[3] = s3;
    };
    
    // As above, block and plaintext are arrays of 4 32-bit words.
    aes.prototype.decryptBlock = function(block, plaintext) {
        if (!this._decryptScheduled) this.scheduleDecrypt();
    
        this.encryptBlock(block, plaintext, true);
    };
    
    
    ////////////////////
    // HELPER FUNCTIONS
    ////////////////////
    
    aes.wordsToBytes = function(words, bytes) {
        var bitmask = 1;
        for (var i=0; i < 7; i++) bitmask = (bitmask << 1) | 1;
        for (var i=0; i < words.length; i++) {
            var bstart = i*4;
            for (var j=0; j < 4; j++) {
                bytes[bstart+j] = (words[i] & (bitmask << (8*(3-j)))) >>> (8*(3-j));
            }
        }
    };
    
    aes.bytesToWords = function(bytes, words) {
        var paddedBytes = bytes.slice();
        while (paddedBytes.length % 4 != 0) paddedBytes.push(0);
        var num_words = Math.floor(paddedBytes.length/4);
        for (var j=0; j < num_words; j++)
            words[j] = ((paddedBytes[(j<<2)+3]) | (paddedBytes[(j<<2)+2] << 8) | (paddedBytes[(j<<2)+1] << 16) | (paddedBytes[j<<2] << 24));
    };
    
    ///////////////////////////////////////
    // ROUND TRANSFORMATION PRECOMPUTATION
    ///////////////////////////////////////
    
    
    // Precomputation code by Mike Hamburg
    
    aes.prototype._precompute = function() {
      var x,xi,sx,tx,tisx,i;
      var d=[];
    
      // compute double table
      for (x=0;x<256;x++) {
        d[x]= x&128 ? x<<1 ^ 0x11b : x<<1;
        //d[x] = x<<1 ^ (x>>7)*0x11b; //but I think that's less clear.
      }
    
      // Compute the round tables.
      // 
      // We'll need access to x and x^-1, which we'll get by walking
      // GF(2^8) as generated by (82,5).
      //
      for(x=xi=0;;) {
        // compute sx := sbox(x)
        sx = xi^ xi<<1 ^ xi<<2 ^ xi<<3 ^ xi<<4;
        sx = sx>>8 ^ sx&0xFF ^ 0x63;
    
        var dsx = d[sx], x2=d[x],x4=d[x2],x8=d[x4];
    
        // te(x) = rotations of (2,1,1,3) * sx
        tx   = dsx<<24 ^ sx<<16 ^ sx<<8 ^ sx^dsx;
    
        // similarly, td(sx) = (E,9,D,B) * x
        tisx = (x8^x4^x2) <<24 ^
               (x8^x    ) <<16 ^
               (x8^x4^x ) << 8 ^
               (x8^x2^x );
    
        // This can be done by multiplication instead but I think that's less clear
        // tisx = x8*0x1010101 ^ x4*0x1000100 ^ x2*0x1000001 ^ x*0x10101;
        // tx = dsx*0x1000001^sx*0x10101;
    
        // rotate and load
        for (i=0;i<4;i++) {
          this._T[i][x]  = tx;
          this._Tin[i][sx] = tisx;
          tx   =   tx<<24 | tx>>>8;
          tisx = tisx<<24 | tisx>>>8;
        }
    
        // te[4] is the sbox; td[4] is its inverse
        this._SBOX[ x] = sx;
        this._INV_SBOX[sx] =  x;
        
        // wonky iteration goes through 0
        if (x==5) {
          break;
        } else if (x) {
          x   = x2^d[d[d[x8^x2]]]; // x  *= 82 = 0b1010010
          xi ^= d[d[xi]];          // xi *= 5  = 0b101
        } else {
          x=xi=1;
        }
      }
    
      // We computed the arrays out of order.  On Firefox, this matters.
      // Compact them.
      for (i=0; i<4; i++) {
        this._T[i] = this._T[i].slice(0);
        this._Tin[i] = this._Tin[i].slice(0);
      }
      this._SBOX = this._SBOX.slice(0);
      this._INV_SBOX = this._INV_SBOX.slice(0);
    
    
    };
    
    aes.prototype.getSbox = function() {
       return this._SBOX.toString();
    }
    
    aes.prototype.getInvSbox = function() {
        return this._INV_SBOX.toString();    
    }
    
    return new aes(key);
    }-*/;
    
    native String getSbox() /*-{ return this.getSbox(); }-*/;
    
    native String getInvSbox() /*-{ return this.getInvSbox(); }-*/;
    
    native JsArrayInteger encrypt(JsArrayInteger blockToEncrypt) 
        /*-{ var ciphertext = []; this.encryptBlock(blockToEncrypt, ciphertext, false); return ciphertext; }-*/;
    
    native JsArrayInteger decrypt(JsArrayInteger blockToDecrypt) 
    /*-{ var plaintext = []; this.decryptBlock(blockToDecrypt, plaintext); return plaintext; }-*/;

    protected JsCryptoNativeAES() { }

}
