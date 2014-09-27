package com.googlecode.cryptogwt.provider;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

public final class JsCryptoSHA256 extends JavaScriptObject {

    static native JsCryptoSHA256 newInstance() /*-{
    // 
    // jsCrypto
    //
    // sha256.js
    // Mike Hamburg, 2008.  Public domain.
    // 
         
      function SHA256() {
//        if (!this.k[0])
//          this.precompute();
        this.initialize();
      }
      SHA256.prototype = {
      
        init:[0x6a09e667,0xbb67ae85,0x3c6ef372,0xa54ff53a,0x510e527f,0x9b05688c,0x1f83d9ab,0x5be0cd19],
      
        k:[0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
           0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
           0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
           0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
           0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
           0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
           0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
           0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2],
        
     
//      init:[], k:[],
//    
//      precompute: function() {
//        var p=2,i=0,j;
//    
//        function frac(x) { return (x-Math.floor(x)) * 4294967296 | 0 }
//    
//        outer: for (;i<64;p++) {
//          for (j=2;j*j<=p;j++)
//        if (p % j == 0)
//          continue outer;
//    
//          if (i<8) this.init[i] = frac(Math.pow(p,1/2));
//          this.k[i] = frac(Math.pow(p,1/3));
//          i++;
//        }
//      },
    
      initialize:function() {
        this.h = this.init.slice(0);
        this.word_buffer   = [];
        this.bit_buffer    = 0;
        this.bits_buffered = 0; 
        this.length        = 0;
        this.length_upper  = 0;
      },
    
      // one cycle of SHA256
      block:function(words) {
        var w=words.slice(0),i,h=this.h,tmp,k=this.k;
    
        var h0=h[0],h1=h[1],h2=h[2],h3=h[3],h4=h[4],h5=h[5],h6=h[6],h7=h[7];
        for (i=0;i<64;i++) {
          if (i<16) {
        tmp=w[i];
          } else {
            var a=w[(i+1)&15], b=w[(i+14)&15];
            tmp=w[i&15]=((a>>>7^a>>>18^a>>>3^a<<25^a<<14) + (b>>>17^b>>>19^b>>>10^b<<15^b<<13) + w[i&15] + w[(i+9)&15]) | 0;
          }
          
          tmp = tmp + h7 + (h4>>>6^h4>>>11^h4>>>25^h4<<26^h4<<21^h4<<7) + (h6 ^ h4&(h5^h6)) + k[i] | 0;
          
          h7=h6; h6=h5; h5=h4;
          h4 = h3 + tmp | 0;
    
          h3=h2; h2=h1; h1=h0;
    
          h0 = (tmp + ((h1&h2)^(h3&(h1^h2))) + (h1>>>2^h1>>>13^h1>>>22^h1<<30^h1<<19^h1<<10)) | 0;
        }
    
        h[0]+=h0; h[1]+=h1; h[2]+=h2; h[3]+=h3;
        h[4]+=h4; h[5]+=h5; h[6]+=h6; h[7]+=h7;
      },
    
      update_word_big_endian:function(word) {
        var bb;
        if ((bb = this.bits_buffered)) {
          this.word_buffer.push(word>>>(32-bb) | this.bit_buffer);
          this.bit_buffer = word << bb;
        } else {
          this.word_buffer.push(word);
        }
        this.length += 32;
        if (this.length == 0) this.length_upper ++; // mmhm..
        if (this.word_buffer.length == 16) {
          this.block(this.word_buffer);
          this.word_buffer = [];
        }
      },
    
      update_word_little_endian:function(word) {
        word = word >>> 16 ^ word << 16;
        word = ((word>>>8) & 0xFF00FF) ^ ((word<<8) & 0xFF00FF00);
        this.update_word_big_endian(word);
      },
    
      update_words_big_endian: function(words) { 
        for (var i=0; i<words.length; i++) this.update_word_big_endian(words[i]);
      },
    
      update_words_little_endian: function(words) { 
        for (var i=0; i<words.length; i++) this.update_word_little_endian(words[i]);
      },
    
      update_byte:function(b) {
        this.bit_buffer |= (b & 0xff) << (24 - (this.bits_buffered));
        this.bits_buffered += 8;
        if (this.bits_buffered == 32) {
          this.bits_buffered = 0; 
          this.update_word_big_endian(this.bit_buffer);
          this.bit_buffer = 0;
        }
      },
    
      update_string:function(string) {
        throw "not yet implemented";
      },
    
      finalize:function() {
        var i, wb = this.word_buffer;
    
        wb.push(this.bit_buffer | (0x1 << (31 - this.bits_buffered)));
        var zeros_to_pad = 16 - wb.length - 2;
        if (zeros_to_pad < 0) zeros_to_pad += 16;
        for (i = 0; i < zeros_to_pad; i++) {
          wb.push(0);          
        }
        
        wb.push(this.length_upper);
        wb.push(this.length + this.bits_buffered);
                
        this.block(wb.slice(0,16));
        if (wb.length > 16) {
          this.block(wb.slice(16,32));
        }
    
        var h = this.h;
        this.initialize();
        h[0] = h[0] | 0; h[1] = h[1] | 0; h[2] = h[2] | 0; h[3] = h[3] | 0;
        h[4] = h[4] | 0; h[5] = h[5] | 0; h[6] = h[6] | 0; h[7] = h[7] | 0;
         
        return h;
      }
    }
    
    return new SHA256();
    }-*/;
    
    protected JsCryptoSHA256() { }
    
    native void update(byte b)
        /*-{ this.update_byte(b); }-*/;
    
    native void update(int word) 
        /*-{ this.update_word_big_endian(word); }-*/;
    
    native void update(JsArrayInteger words) 
        /*-{ this.update_words_big_endian(words); }-*/;
    
    native JsArrayInteger digest() /*-{ return this.finalize(); }-*/;

}
