TestSpaceWrite : UnitTest {
  classvar
    <>manual = false
  ;
  var
    tmp, sounds, write, linemap, tree, data, str, draw
  ;

  *new {
    ^super.new.init;
  }
  
  // Run one test with debug output on (manual=true), but
  // run all tests before to immediately see if fixing
  // one test broke the others
  *runManual { |test, runAllFirst = false|
    var m = manual;
      this.forkIfNeeded {
      manual = false;
      if (runAllFirst) {
        this.run;
      };
      manual = true;
      this.runTest(this.name++":test_"++test);
      manual = m;
    }
  }

	init { 
    tmp = SpaceTmp.new;
    linemap = SpaceLinemap(\drum);
    tree = SpaceTree();
  }

  todo { |title, msg|
    "".postln;
    ("TODO:"+currentMethod+$-+title).postln;
    "".postln;
    (msg).postln;
    "".postln;
    "".postln;
  }

  analyze {
    sounds=[];
    data.do {
      arg voiceData, i;
      var sound;
      sound = SoundFile(tmp.file("wav") ++ if(i==0, "", $.++i));
      sound.headerFormat="WAV";
      sound.numChannels=3;
      
      sound.openWrite;
      sound.writeData(voiceData);
      sound.close;

      sounds=sounds.add(sound);
    };

    write = SpaceWrite(sounds.copy, tree, linemap); 
    write.analyze;
    
    if (manual) {
      draw = StDraw(data, 3, currentMethod.asString.split($_).last); 
      "".postln;
      "DATA".postln;
      TestSpaceWriteData.abs(data).asCompileString.postln;
      "".postln;
      "".postln;
      "SECTIONS".postln;
      write.sections.postln;
      "".postln;
      "".postln;
    }
  }

  write {
    tree.path = tmp.file(\drum);
    protect  {
      write.apply;
    } {
    
      block {
        var f;
        if (File.exists(tree.path)) {
          f = File.open(tree.path, "r");
          str = f.readAllString;
          f.close;
        }{
          str=""; 
          "% does not exist".format(tree.path).warn;
        };
      };
      if (manual) {
        "".postln;
        "STFILE".postln;
        str.postln;
        "".postln;
        "".postln;
        block {
          var p,f;
          p = "/home/carlo/hoodie/phrase/drum/a/note.drum";
          File.delete(p);
          f=File.open(p, "w");
          f.write(str);
          f.close;
        };
      };
    };
  }

  assertSections {
    |sections|
    // See assertLength for reason for asString
    this.assertEquals(write.sections.collect {|e|e.asString}, sections.collect{|e|e.asString}, "sections");
  }

  assertLength {
    |length|
    // Compare as string to avoid unexpected rounding behavior
    // https://github.com/supercollider/supercollider/issues/1875
    // This is okay because SpaceTracker is a string format
    this.assertEquals(write.length.asString, length.asString, "length");
  }

  assertStr { |argStr|
    this.assertEquals(str, argStr, 'spacetracker');
  }

  test_soundsDo {
    var sound, data;
    data = FloatArray[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2];
    sound = SoundFile(tmp.file("wav"));
    sound.headerFormat="WAV";
    sound.numChannels=3;
    
    sound.openWrite;
    sound.writeData(data);
    sound.close;

    write = SpaceWrite([sound]);

    write.soundsDo {
      |consume, line|
      this.assertEquals(line, data[0..2]);
      data=data.drop(3);
      consume.value(0);
    };
  }

  test_soundsDo_merge {
    var sound, data, mergedData, compareData;
if (manual==false) {^this};
    data = FloatArray[
      0.5, 0, 0.5,
      0.5, 0, 0.5,
      0.5, 0, 0.5,
      0.5, 1, 0.5,
      0.5, 2, 0.5,
      0.5, 3, 0.5,
      0.5, 0, 0.5,
      0.5, 0, 0.5,
      0.5, 0, 0.5,
      0.5, 1, 0.5,
      0.5, 2, 0.5,
      0.5, 3, 0.5
    ];
    compareData = FloatArray[1.5, 0, 0.5, 1.5, 1, 0.5,1.5, 0, 0.5, 1.5, 1, 0.5];
    sound = SoundFile(tmp.file("wav"));
    sound.headerFormat="WAV";
    sound.numChannels=3;
    
    sound.openWrite;
    sound.writeData(data);
    sound.close;

    write = SpaceWrite([sound]);

    mergedData = FloatArray[];
    write.soundsDo ({
      |consume, line|
      mergedData = mergedData ++ line;
      consume.value(0);
    }, merge: true);
  
    this.assertEquals(compareData, mergedData, 'merged data');
  }

  test_single {
    
    data = [
      FloatArray[
        0.25, 36, 0.5,
        0.25, 40, 0.5,
        0.25, 42, 0.5,
        0.25, 40, 0.5,
        0.25, 36, 0.5,
        0.25, 40, 0.5,
        0.25, 42, 0.5,
        0.25, 40, 0.5,
      ]
    ];
    
    this.analyze;

    this.assertSections([ false, 0]);
    this.assertLength(2);
    
    this.write;
    /*

    this.assertEquals(str.findAll("\n").size, 8);

    str.split($\n).do { |line, i|
      var a, b, note, vel;
      
      if (line.size != 0) {
        #a, b, note, vel = line.split($ );

        this.assertEquals( (a.asFloat / b.asFloat) * 4, 0.25);
        this.assertEquals( ["kick", "snare", "hat"]
.foldAt(i), note);
        this.assertEquals( vel.asFloat, 0.5);
      };
    };
    */

  }
 
  test_overlapping {
if (manual==false) {^this};
  
    data = [
      FloatArray[
        2, 0, 0,
        1.5, 36, 0.5, //kick
        0.5, 0, 0,
        1.5, 37, 0.5, //rim
        0.5, 0, 0,
        1.5, 38, 0.5, //snarer               // This channel is consumed before end of section
      ],
      FloatArray[
        2.5, 0, 0,
        1, 40, 0.5,   //snare
        1, 0, 0,
        1, 41, 0.5    //floor                  // This one too
      ],
      FloatArray[
        3, 0, 0,
        0.5, 42, 0.5, //hat
        1.5, 0, 0,                   // Because this channel is longer than the other two
        0.5, 43, 0.5, //ceil
        //1.5, 0, 0, 
      ]

    ];
    
    this.analyze;
   
    this.assertSections([ false, 0, true, 2, false, 3.5, true, 4, false, 5.5 ]);
    this.assertLength(7.5);

    this.write;

    //this.assert( write.sections.asArray == [ false, 0, true, 2, true, 3.5, false, 6 ], "sections correct");

    //str.findRegexp("^[^ ]").postln;

  }
  
  test_uneven {
if (manual==false) {^this};
  
    data = [
      FloatArray[
        0.21, 0, 0,
        0.2, 36, 0.5,
        0.2,0,0
      ],
      FloatArray[
        0.42, 0, 0,
        0.2, 40, 0.5,
        0.2, 0, 0
      ],
      FloatArray[
        0.43, 0, 0,
        0.2, 42, 0.5,
        0.2,0,0,
        0.2,42,0,
        0.2,0,0
      ]
    ];

    this.analyze;
    
    this.assertSections([ false, 0, true, 0.42, false, 0.63 ]);
    this.assertLength(0);

    this.write;

  }

  test_recorded {
if (manual==false) {^this};
    
    data = [
      FloatArray[ 0.54666668176651, 0.0, 0.0, 0.39333334565163, 36.0, 0.46875, 0.19733333587646, 0.0, 0.0, 0.195999994874, 36.0, 0.40104165673256, 1.1786667108536, 0.0, 0.0, 0.27599999308586, 42.0, 0.5, 0.10800000280142, 0.0, 0.0, 0.4040000140667, 40.0, 0.421875, 0.4066666662693, 42.0, 0.64583331346512, 0.41200000047684, 36.0, 0.51041668653488, 0.27066665887833, 0.0, 0.0, 0.16266666352749, 36.0, 0.41666665673256, 0.33599999547005, 0.0, 0.0, 0.24400000274181, 42.0, 0.55208331346512, 0.53600001335144, 0.0, 0.0, 0.28666666150093, 42.0, 0.578125, 0.15199999511242, 0.0, 0.0, 0.37200000882149, 40.0, 0.52604168653488, 0.41333332657814, 42.0, 0.52604168653488, 0.0040000001899898, 0.0, 0.0, 0.44666665792465, 36.0, 0.53125 ],
      FloatArray[ 0.93999999761581, 0.0, 0.0, 0.19733333587646, 42.0, 0.484375, 0.17066666483879, 0.0, 0.0, 0.36133334040642, 40.0, 0.48958334326744, 0.0253333337605, 0.0, 0.0, 0.11200000345707, 42.0, 0.49479165673256, 0.11200000345707, 0.0, 0.0, 0.3120000064373, 36.0, 0.39583334326744, 0.10800000280142, 0.0, 0.0, 0.19733333587646, 36.0, 0.390625, 0.21333333849907, 0.0, 0.0, 0.17733334004879, 36.0, 0.38541665673256, 1.1879999637604, 0.0, 0.0, 0.2986666560173, 42.0, 0.61458331346512, 0.094666667282581, 0.0, 0.0, 0.3906666636467, 40.0, 0.5, 0.23333333432674, 0.0, 0.0, 0.2986666560173, 36.0, 0.48958334326744, 0.082666665315628, 0.0, 0.0, 0.16666667163372, 36.0, 0.43229165673256, 0.26933333277702, 0.0, 0.0 ]
    ];

    //data = data.collect {|a| a[0..11]};
    
    this.analyze;
  
    this.assertSections([]);
    this.assertLength(0);

    this.write;
  }

  test_typed {
  
    data = [
      FloatArray[
        1, 32, 0.5,
        1.5,0,0,
        0.5,32,0
      ],
      FloatArray[
        1.5, 40, 0.5,
        1,0,0,
        1.5,40, 0
      ],
      FloatArray[
        2, 42, 0.5,
        0.5, 0, 0,
        1, 42, 0
      ]
    ];

    this.analyze;
    
    this.assertSections([ true, 0, false, 2, true, 2.5 ]);
    this.assertLength(4);
    
    this.write;

    this.assertStr(
" 1 4 32 0.5
  1 4 0 0
 1.5 4 snare 0.5
  0.5 4 0 0
 2 4 hat 0.5
0.5 4 0 0
 0.5 4 32 0
 1.5 4 snare 0
 1 4 hat 0
"
    );

  }

  test_random {
if (manual==false) {^this};
    data = [ FloatArray[ 1.1824239492416, 0.0, 0.5, 1.3132796287537, 0.0, 0.5, 2.2417387962341, 0.0, 0.5, 1.2090611457825, 0.0, 0.5, 1.3552812337875, 0.0, 0.5, 1.6147516965866, 40.0, 0.5, 1.2977979183197, 0.0, 0.5, 0.48680591583252, 36.0, 0.5, 1.3031941652298, 0.0, 0.5, 0.52307653427124, 0.0, 0.5, 0.73462736606598, 0.0, 0.5, 2.7826247215271, 0.0, 0.5, 0.67553758621216, 36.0, 0.5, 1.7511359453201, 0.0, 0.5 ], FloatArray[ 2.2373192310333, 42.0, 0.5, 0.9502158164978, 40.0, 0.5, 2.525218963623, 0.0, 0.5, 1.6359100341797, 0.0, 0.5, 0.52730655670166, 42.0, 0.5, 0.78134250640869, 36.0, 0.5, 2.2115156650543, 42.0, 0.5, 0.98762655258179, 36.0, 0.5, 0.55391371250153, 0.0, 0.5, 1.2786355018616, 42.0, 0.5 ], FloatArray[ 1.8387615680695, 36.0, 0.5, 1.6246987581253, 0.0, 0.5, 2.897008895874, 0.0, 0.5, 0.45647156238556, 0.0, 0.5, 0.35529327392578, 0.0, 0.5, 0.95848023891449, 0.0, 0.5, 2.568336725235, 42.0, 0.5, 1.8173797130585, 36.0, 0.5, 2.3499019145966, 0.0, 0.5, 2.2791645526886, 36.0, 0.5, 2.671049118042, 0.0, 0.5, 0.85989725589752, 42.0, 0.5 ], FloatArray[ 2.1788067817688, 42.0, 0.5, 2.2879028320312, 36.0, 0.5, 1.8371851444244, 0.0, 0.5, 1.7150316238403, 0.0, 0.5, 0.85236346721649, 0.0, 0.5, 0.2524641752243, 0.0, 0.5, 1.8831199407578, 0.0, 0.5 ], FloatArray[ 2.6700320243835, 42.0, 0.5, 1.3452444076538, 36.0, 0.5, 0.41943347454071, 0.0, 0.5, 1.4358240365982, 40.0, 0.5, 0.44873893260956, 0.0, 0.5, 0.27519464492798, 0.0, 0.5, 0.54291987419128, 0.0, 0.5, 1.138622045517, 0.0, 0.5, 2.299925327301, 40.0, 0.5, 0.67224955558777, 0.0, 0.5, 1.0139719247818, 42.0, 0.5 ], FloatArray[ 1.7064943313599, 0.0, 0.5, 2.1462645530701, 0.0, 0.5, 2.5233249664307, 0.0, 0.5, 2.2044577598572, 42.0, 0.5, 1.1415467262268, 40.0, 0.5, 0.93629658222198, 40.0, 0.5, 1.804839849472, 0.0, 0.5, 0.1718498468399, 0.0, 0.5 ], FloatArray[ 0.68408095836639, 0.0, 0.5, 2.0180189609528, 42.0, 0.5, 2.3436989784241, 0.0, 0.5, 0.98450410366058, 0.0, 0.5, 1.5085297822952, 36.0, 0.5 ], FloatArray[ 0.81333804130554, 40.0, 0.5, 2.5325553417206, 42.0, 0.5, 2.8141765594482, 0.0, 0.5, 2.2496509552002, 36.0, 0.5, 2.5075082778931, 36.0, 0.5, 0.63715660572052, 0.0, 0.5 ], FloatArray[ 1.2480726242065, 42.0, 0.5, 0.33731245994568, 40.0, 0.5, 0.78520023822784, 36.0, 0.5, 0.76668477058411, 42.0, 0.5, 2.880735874176, 0.0, 0.5 ], FloatArray[ 2.4313008785248, 0.0, 0.5, 2.6890707015991, 36.0, 0.5, 0.22757005691528, 36.0, 0.5, 1.3468930721283, 42.0, 0.5, 0.013934254646301, 0.0, 0.5, 0.20512497425079, 0.0, 0.5 ] ];

    this.analyze;
    
    this.assertSections([ false, 0, true, 0.5, false, 2.5, true, 3, false, 4.5 ]);
    this.assertLength(0);
 
    this.write

  }

  test_random2 {
if (manual==false) {^this};
    data = [ FloatArray[ 1.7024188041687, 40.0, 0.5, 1.313708782196, 42.0, 0.5, 2.3938364982605, 42.0, 0.5, 1.7928360700607, 0.0, 0.5, 1.1613364219666, 36.0, 0.5, 1.056040763855, 0.0, 0.5, 0.092543363571167, 36.0, 0.5 ], FloatArray[ 2.0534439086914, 40.0, 0.5, 0.48769855499268, 0.0, 0.5, 0.17017114162445, 0.0, 0.5, 1.2454122304916, 42.0, 0.5, 2.7214341163635, 36.0, 0.5, 0.59098505973816, 0.0, 0.5, 2.6774210929871, 42.0, 0.5, 0.64816868305206, 0.0, 0.5, 0.90229690074921, 0.0, 0.5, 1.8730298280716, 0.0, 0.5 ], FloatArray[ 1.0841202735901, 0.0, 0.5, 1.9946125745773, 0.0, 0.5, 0.81156170368195, 40.0, 0.5, 2.6649994850159, 36.0, 0.5, 1.6451400518417, 0.0, 0.5, 0.93609416484833, 0.0, 0.5, 1.2707419395447, 0.0, 0.5, 2.6808214187622, 0.0, 0.5, 2.0809369087219, 40.0, 0.5, 0.78767788410187, 0.0, 0.5, 0.87845242023468, 0.0, 0.5, 0.14110994338989, 0.0, 0.5 ], FloatArray[ 0.44940054416656, 42.0, 0.5, 0.24749028682709, 0.0, 0.5, 2.5281257629395, 36.0, 0.5, 2.2098217010498, 40.0, 0.5, 0.76918709278107, 0.0, 0.5, 1.3502204418182, 42.0, 0.5, 0.70447611808777, 0.0, 0.5 ], FloatArray[ 0.5160870552063, 36.0, 0.5, 0.88781297206879, 40.0, 0.5, 1.4044321775436, 36.0, 0.5, 2.4139428138733, 42.0, 0.5, 1.643252491951, 0.0, 0.5, 1.8641374111176, 40.0, 0.5, 2.5157442092896, 42.0, 0.5, 2.2349152565002, 0.0, 0.5, 1.3399583101273, 42.0, 0.5, 0.068712472915649, 0.0, 0.5, 1.0650247335434, 0.0, 0.5 ], FloatArray[ 1.1728674173355, 0.0, 0.5, 2.9477124214172, 0.0, 0.5, 0.70783638954163, 36.0, 0.5, 0.86824107170105, 0.0, 0.5, 0.77077424526215, 0.0, 0.5 ], FloatArray[ 2.640576839447, 36.0, 0.5, 1.1183924674988, 0.0, 0.5, 2.7703785896301, 40.0, 0.5, 1.4471129179001, 0.0, 0.5, 2.6861362457275, 0.0, 0.5, 0.30143129825592, 0.0, 0.5, 1.3061953783035, 40.0, 0.5, 0.059484958648682, 0.0, 0.5, 1.7930384874344, 42.0, 0.5 ], FloatArray[ 1.5269029140472, 0.0, 0.5, 0.0032486915588379, 0.0, 0.5, 1.2396687269211, 0.0, 0.5, 0.44042372703552, 36.0, 0.5, 1.0356631278992, 0.0, 0.5, 1.0742965936661, 0.0, 0.5, 2.289256811142, 0.0, 0.5, 0.36064982414246, 0.0, 0.5, 0.090297102928162, 40.0, 0.5, 2.1730737686157, 0.0, 0.5, 1.6727271080017, 0.0, 0.5, 2.15558385849, 40.0, 0.5, 1.2080011367798, 42.0, 0.5 ], FloatArray[ 0.1565283536911, 0.0, 0.5, 1.6232106685638, 36.0, 0.5, 0.75233638286591, 40.0, 0.5, 2.6861596107483, 0.0, 0.5, 0.67354452610016, 42.0, 0.5, 1.7990856170654, 36.0, 0.5 ], FloatArray[ 2.8735818862915, 0.0, 0.5, 1.8519769906998, 36.0, 0.5, 1.9972125291824, 36.0, 0.5, 2.0040173530579, 42.0, 0.5, 1.1770677566528, 42.0, 0.5, 1.8772215843201, 36.0, 0.5, 2.9143009185791, 0.0, 0.5, 2.5210962295532, 0.0, 0.5, 0.57000696659088, 40.0, 0.5, 2.3966453075409, 0.0, 0.5, 2.6196942329407, 0.0, 0.5 ] ];

    this.analyze;

    this.assertSections([]);
    this.assertLength(0);

    this.write;
  }

  test_random3 {
if (manual==false) {^this};
    data = [ FloatArray[ 1.7618418931961, 42.0, 0.5, 1.0410937070847, 36.0, 0.5, 2.4144511222839, 0.0, 0.5, 0.59898233413696, 0.0, 0.5, 1.3181179761887, 0.0, 0.5, 2.9570109844208, 36.0, 0.5, 2.0630879402161, 0.0, 0.5, 0.34258317947388, 42.0, 0.5, 2.4729130268097, 0.0, 0.5, 0.53614175319672, 40.0, 0.5 ], FloatArray[ 2.7916970252991, 42.0, 0.5, 0.63721883296967, 0.0, 0.5, 1.8033310174942, 40.0, 0.5, 1.2512140274048, 36.0, 0.5, 2.9620757102966, 42.0, 0.5, 1.2521667480469, 0.0, 0.5, 2.3004734516144, 0.0, 0.5, 0.24417364597321, 36.0, 0.5, 1.2415874004364, 0.0, 0.5, 0.69378697872162, 0.0, 0.5, 2.4861540794373, 0.0, 0.5 ], FloatArray[ 0.72829341888428, 0.0, 0.5, 1.7470554113388, 42.0, 0.5, 2.6375505924225, 0.0, 0.5, 1.3654071092606, 36.0, 0.5, 0.13786053657532, 42.0, 0.5, 1.2890177965164, 0.0, 0.5, 0.99530839920044, 0.0, 0.5, 0.53148472309113, 0.0, 0.5, 0.35352909564972, 36.0, 0.5, 1.5656558275223, 0.0, 0.5, 2.1735501289368, 36.0, 0.5, 2.4895784854889, 36.0, 0.5 ] ];

    this.analyze;
   
    this.assertSections([]);
    this.assertLength(0);

    this.write;
  }

  test_random4 {
if (manual==false) {^this};
    data = [ FloatArray[ 0.48788809776306, 0.0, 0.5, 2.5708627700806, 0.0, 0.5, 2.4670708179474, 0.0, 0.5, 1.7356063127518, 0.0, 0.5, 1.0764698982239, 0.0, 0.5, 0.97416615486145, 42.0, 0.5 ], FloatArray[ 1.3373662233353, 0.0, 0.5, 2.7281284332275, 0.0, 0.5, 1.7484973669052, 36.0, 0.5, 0.8468474149704, 0.0, 0.5, 0.90775144100189, 0.0, 0.5, 2.5934293270111, 0.0, 0.5, 1.1205224990845, 0.0, 0.5 ], FloatArray[ 0.51271283626556, 0.0, 0.5, 2.3531792163849, 0.0, 0.5, 0.11452782154083, 36.0, 0.5, 0.44738459587097, 0.0, 0.5, 0.54803967475891, 0.0, 0.5, 1.7476712465286, 0.0, 0.5, 0.28566205501556, 0.0, 0.5, 1.1721067428589, 0.0, 0.5, 0.30645668506622, 0.0, 0.5, 0.32808673381805, 36.0, 0.5, 2.9876441955566, 0.0, 0.5, 1.0809245109558, 0.0, 0.5 ] ];

    this.analyze;
    
    this.assertSections([]);
    this.assertLength(0);

    this.write;
  }

  test_random5 {
if (manual==false) {^this};
    data=[ FloatArray[ 2.236377954483, 0.0, 0.5, 2.2240462303162, 0.0, 0.5, 0.52190887928009, 0.0, 0.5, 2.1943368911743, 42.0, 0.5, 2.6591014862061, 0.0, 0.5, 0.94408106803894, 42.0, 0.5, 1.2579259872437, 0.0, 0.5, 1.6729402542114, 0.0, 0.5, 2.4004049301147, 0.0, 0.5 ], FloatArray[ 0.81498312950134, 0.0, 0.5, 2.3378205299377, 0.0, 0.5, 2.5445873737335, 42.0, 0.5, 0.25208866596222, 0.0, 0.5, 0.48367559909821, 36.0, 0.5, 0.58576941490173, 0.0, 0.5 ], FloatArray[ 0.14571404457092, 0.0, 0.5, 1.4568257331848, 40.0, 0.5, 1.5670076608658, 0.0, 0.5, 2.4130187034607, 0.0, 0.5, 0.25682079792023, 0.0, 0.5, 2.1527829170227, 0.0, 0.5, 0.0085426568984985, 36.0, 0.5 ], FloatArray[ 1.6110616922379, 0.0, 0.5, 1.4334958791733, 0.0, 0.5, 2.9300923347473, 0.0, 0.5, 2.1918406486511, 0.0, 0.5, 1.0591832399368, 0.0, 0.5, 1.8748687505722, 0.0, 0.5, 0.48366165161133, 0.0, 0.5, 1.5092318058014, 40.0, 0.5 ], FloatArray[ 1.7598863840103, 0.0, 0.5, 1.9409025907516, 40.0, 0.5, 0.28282284736633, 0.0, 0.5, 1.8135970830917, 0.0, 0.5, 1.3214432001114, 0.0, 0.5, 1.1829074621201, 0.0, 0.5, 2.6538171768188, 0.0, 0.5, 1.866739153862, 0.0, 0.5 ], FloatArray[ 1.8893655538559, 0.0, 0.5, 1.9854258298874, 0.0, 0.5, 0.89956676959991, 0.0, 0.5, 1.7844425439835, 0.0, 0.5, 0.69557476043701, 0.0, 0.5, 1.6618680953979, 0.0, 0.5, 0.60180294513702, 0.0, 0.5, 0.83879828453064, 0.0, 0.5 ], FloatArray[ 0.53513431549072, 0.0, 0.5, 1.4911172389984, 0.0, 0.5, 0.71184825897217, 0.0, 0.5, 1.6458513736725, 0.0, 0.5, 2.0658445358276, 0.0, 0.5, 0.31670415401459, 0.0, 0.5, 2.7421431541443, 0.0, 0.5, 1.5025012493134, 36.0, 0.5, 2.7446229457855, 0.0, 0.5, 1.0956931114197, 0.0, 0.5, 1.1660242080688, 0.0, 0.5, 1.9749104976654, 42.0, 0.5, 2.2430067062378, 0.0, 0.5 ], FloatArray[ 2.5928425788879, 0.0, 0.5, 2.0612876415253, 0.0, 0.5, 1.5488841533661, 0.0, 0.5, 2.76442694664, 0.0, 0.5, 0.089459896087646, 0.0, 0.5, 0.42482650279999, 42.0, 0.5, 0.70355379581451, 42.0, 0.5, 2.633721113205, 42.0, 0.5 ], FloatArray[ 1.0060354471207, 0.0, 0.5, 1.8368961811066, 42.0, 0.5, 2.3551850318909, 0.0, 0.5, 2.4737215042114, 0.0, 0.5, 1.4581643342972, 0.0, 0.5, 2.0351715087891, 0.0, 0.5, 2.2257962226868, 0.0, 0.5, 1.3658566474915, 42.0, 0.5, 0.47999668121338, 0.0, 0.5, 0.94090247154236, 0.0, 0.5 ], FloatArray[ 0.45810127258301, 0.0, 0.5, 2.758716583252, 0.0, 0.5, 1.5087801218033, 0.0, 0.5, 2.9927794933319, 0.0, 0.5, 0.36483693122864, 36.0, 0.5, 2.5065279006958, 36.0, 0.5 ] ];
  
    this.analyze;
    
    this.assertSections([]);
    this.assertLength(0);
    
    this.write;
  }

  test_multimulti {
if (manual==false) {^this};
    data=[
      FloatArray[
        1,36,0.5,
        1,36,0.5,
        1,36,0.5,
        1,42,0.5,
        1.5,42,0.5,
        1.5,42,0.5
      ],
      FloatArray[
        1,40,0.5,
        1,40,0.5,
        1,40,0.5,
        2,40,0.5,
        2,40,0.5
      ],
      FloatArray[
        1,42,0.5,
        1,42,0.5,
        1.5,42,0.5,
        1.5,42,0.5,
        2,42,0.5
      ]
    ];

    this.todo("parallel split", "Split parallel sections into different parallel sections when begins line up");

    this.analyze;
    
    this.assertSections([true, 0]);
    this.assertLength(7);
  
    this.write;
  
    this.assertStr(" 1 4 kick 0.5
  1 4 kick 0.5
  1 4 kick 0.5
  1 4 hat 0.5
  1.5 4 hat 0.5
  1.5 4 hat 0.5
 1 4 snare 0.5
  1 4 snare 0.5
  1 4 snare 0.5
  2 4 snare 0.5
  2 4 snare 0.5
 1 4 hat 0.5
  1 4 hat 0.5
  1.5 4 hat 0.5
  1.5 4 hat 0.5
  2 4 hat 0.5
");
  }
 
}


TestSpaceWriteUtil {
  *serialize {
    arg path;
    var i, data, d, f;

    data = []
;
    
    i=0;
    while {
      f = SoundFile();
      f.openRead(path ++ if(i==0,"",($.++i)));
    }{
      d = FloatArray.newClear(1024 * f.numChannels);
      f.readData(d);
      f.close;
      i=i+1;
      data=data.add(d);
    };

    ^data.asCompileString;
  }


}

TestSpaceWriteCompare {

  var
    <>str,
    <>comparableFormat
  ;

  *new {
    arg str;
    ^super.newCopyArgs(str).init;
  }

  init {
  }

  == {
    arg another;
    ^another.comparableFormat == comparableFormat;
  }

}

TestSpaceWriteData {
 
  var
    <>averagePausesPerNote,
    <>polyphony,
    <>notes,
    <>data
  ;

  *new {
    arg averagePausesPerNote = 1, polyphony = 3;
    ^super.newCopyArgs(averagePausesPerNote, polyphony).init;
  }

  init {
   notes = [36,40,42];
 }

  line {
    var zeroes = Array.fill((notes.size*averagePausesPerNote).round, 0);
    ^FloatArray[3.0.rand, (notes++zeroes).choose, 0.5];
  }

  generate {
    data=polyphony.collect{
      var stream = FloatArray[];
      (5+10.rand).do {
        stream = stream ++ this.line;
      };
      stream;
    };
    ^data;
  }

  *abs {
    arg data, numChannels = 3;
    ^data.collect {|ch|
      ch = ch.clump(numChannels);
      (ch.size - 1).do {|i|
        ch[i+1][0] = ch[i+1][0] + ch[i][0];
      };
      ch.flatten;
    };
  }

}

+ Object {
  postm {
    if(TestSpaceWrite.manual) {
      this.postln;
    };
  }
}


