TestPlayBufS : UnitTest {
  
  var
    s
  ;
  
  setUp {
    s = Server.default;
    this.bootServer(s);
  }

  test_play {
  
    var bufferS, synth, buffer, data;

    SynthDef(\TestPlayBufS, {|rate=0, t_trig=0, start=0, buffer, bufferS|
      var control;
      control = PlayBufS.kr(2, bufferS, rate, t_trig, start, 2);
      RecordBuf.kr(control, buffer, run: rate>0);
    }).send(s);
    s.sync;

    bufferS = Buffer.loadCollection(s, [
      0.01, 48, 0.01,
      0.02, 59, 0.02,
      0.03, 50, 0.03,
      0.04, 51, 0.04,
      0.05, 52, 0.05,
    ], 3);
    
    buffer = Buffer.alloc(s, 256, 2); 
    
    OSCFunc({
      buffer.getn(0, 512, {|d|
        //d.asCompileString.post
        data = d;
      });
    }, '/n_end', s.addr).oneShot;
  
    s.sync;

    Synth(\TestPlayBufS, [\rate, 1, \buffer, buffer.bufnum, \bufferS, bufferS.bufnum], s);
    
    this.asynchAssert({ data.notNil }, {
      this.assertEquals(data.round(0.00001), [ 48.0, 0.0099999997764826, 48.0, 0.0099999997764826, 48.0, 0.0099999997764826, 48.0, 0.0099999997764826, 48.0, 0.0099999997764826, 48.0, 0.0099999997764826, 0.0, 0.0, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 59.0, 0.019999999552965, 0.0, 0.0, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 50.0, 0.029999999329448, 0.0, 0.0, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 51.0, 0.03999999910593, 0.0, 0.0, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 52.0, 0.050000000745058, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ].round(0.00001));
    }, "data timeout", 1);
   
  }

}

TestRecordBufS : UnitTest {
  
  var
    s
  ;
  
  setUp {
    s = Server.default;
    this.bootServer(s);
  }

  tearDown {
  }

  test_record {
  
    var bufferS, synth, buffer, data;
    
    SynthDef(\TestRecordBufS, {|rate=0, t_trig=0, start=0, buffer, bufferS|
      var control;
      control = PlayBuf.kr(2, buffer, rate, t_trig, start);
      RecordBufS.kr([control], bufferS, rate>0, 2);
    }).send(s);
    s.sync;

    buffer = Buffer.loadCollection(s,
      []
      ++ [48, 0.1].wrapExtend(24)
      ++ [49, 0.2].wrapExtend(2*24)
      ++ [50, 0.3].wrapExtend(3*24)
      ++ [51, 0.4].wrapExtend(4*24)
      ++ [52, 0.5].wrapExtend(5*24)
      ++ [10, 0.05].wrapExtend(5*2) // trigger done, will not be recorded
    , 2);
    
    bufferS = Buffer.alloc(s, 5, 3);
    
    OSCFunc({
      bufferS.getn(0, 15, {|d|
        //d.asCompileString.post;
        data = d;
      });
    }, '/n_end', s.addr).oneShot;
    
    s.sync;
    
    Synth(\TestRecordBufS, [\rate, 1, \buffer, buffer.bufnum, \bufferS, bufferS.bufnum], s);
    
    this.asynchAssert({ data.notNil }, {
      //data.asCompileString.post;
      this.assertEquals(data.round(0.00001), [ 0.016000000759959, 48.0, 0.10000000149012, 0.032000001519918, 49.0, 0.20000000298023, 0.048000000417233, 50.0, 0.30000001192093, 0.064000003039837, 51.0, 0.40000000596046, 0.079999998211861, 52.0, 0.5 ].round(0.00001));
    }, "data timeout", 1); 
  }
  
  // Free before RecordBufS is done
  test_cutoff {
  
    var bufferS, synth, buffer, data;
    // DoneAction in PlayBuf, not in RecordBufS
    SynthDef(\TestRecordBufS, {|rate=0, t_trig=0, start=0, buffer, bufferS|
      var control;
      control = PlayBuf.kr(2, buffer, rate, t_trig, start, 0, 2);
      RecordBufS.kr([control], bufferS, rate>0);
    }).send(s);
    s.sync;
    buffer = Buffer.loadCollection(s,
      []
      ++ [48, 0.1].wrapExtend(24)
      ++ [49, 0.2].wrapExtend(2*24)
      ++ [50, 0.3].wrapExtend(3*24)
      ++ [51, 0.4].wrapExtend(4*24)
      ++ [52, 0.5].wrapExtend(5*24)
    , 2);
    
    bufferS = Buffer.alloc(s, 4, 3);
    
    OSCFunc({
      bufferS.getn(0, 12, {|d|
        d.asCompileString.post;
        data = d;
      });
    }, '/n_end', s.addr).oneShot;
    
    s.sync;
    Synth(\TestRecordBufS, [\rate, 1, \buffer, buffer.bufnum, \bufferS, bufferS.bufnum], s);
    
    this.asynchAssert({ data.notNil }, {
      this.assertEquals(data.round(0.00001), [ 0.016000000759959, 48.0, 0.10000000149012, 0.032000001519918, 49.0, 0.20000000298023, 0.048000000417233, 50.0, 0.30000001192093, 0.064000003039837, 51.0, 0.40000000596046].round(0.00001));
    }, "data timeout", 1); 
  }

}
