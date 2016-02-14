TestSpaceWrite : UnitTest {

  var
    tmp, sounds, write, linemap, tree, data, str
  ;

  *new {
    ^super.new.init;
  }

	init { 
    tmp = SpaceTmp.new;
    sounds = [];
    linemap = SpaceLinemap(\drum);
    tree = SpaceTree();
    tree.path = tmp.file(\drum);
  }

  analyze {
    var path = tmp.file("wav");
    data.do {
      arg voiceData, i;
      var sound;
      sound = SoundFile(path ++ if(i==0, "", $.++i));
      sound.headerFormat="WAV";
      sound.numChannels=3;
      
      sound.openWrite;
      sound.writeData(voiceData);
      sound.close;

      sounds=sounds.add(sound);
    };

    write = SpaceWrite(sounds, tree, linemap); 
    write.analyze;
    write.sections.postln;
  }

  write {
    protect  {
      write.apply;
    } {
    
      block {
        var f;
        f = File.open(tree.path, "r");
        str = f.readAllString;
        f.close;
      };
      str.postln;
      block {
        var p,f;
        p = "/home/carlo/hoodie/phrase/drum/a/note.drum";
        File.delete(p);
        f=File.open(p, "w");
        f.write(str);
        f.close;
      };
    };
  }

  assertSections {
    |sections|
    this.assertEquals(write.sections.asArray, sections.asArray, 'sections');
  }

  assertLength {
    |length|
    this.assertEquals(write.length, length);
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
    /*
      this.write;

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

    //.write;

    //this.assert( write.sections.asArray == [ false, 0, true, 2, true, 3.5, false, 6 ], "sections correct");

    //str.findRegexp("^[^ ]").postln;

  }
  
  test_uneven {
  
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

  }

  test_recorded {
    
    data = [
      FloatArray[ 0.54666668176651, 0.0, 0.0, 0.39333334565163, 36.0, 0.46875, 0.19733333587646, 0.0, 0.0, 0.195999994874, 36.0, 0.40104165673256, 1.1786667108536, 0.0, 0.0, 0.27599999308586, 42.0, 0.5, 0.10800000280142, 0.0, 0.0, 0.4040000140667, 40.0, 0.421875, 0.4066666662693, 42.0, 0.64583331346512, 0.41200000047684, 36.0, 0.51041668653488, 0.27066665887833, 0.0, 0.0, 0.16266666352749, 36.0, 0.41666665673256, 0.33599999547005, 0.0, 0.0, 0.24400000274181, 42.0, 0.55208331346512, 0.53600001335144, 0.0, 0.0, 0.28666666150093, 42.0, 0.578125, 0.15199999511242, 0.0, 0.0, 0.37200000882149, 40.0, 0.52604168653488, 0.41333332657814, 42.0, 0.52604168653488, 0.0040000001899898, 0.0, 0.0, 0.44666665792465, 36.0, 0.53125 ],
      FloatArray[ 0.93999999761581, 0.0, 0.0, 0.19733333587646, 42.0, 0.484375, 0.17066666483879, 0.0, 0.0, 0.36133334040642, 40.0, 0.48958334326744, 0.0253333337605, 0.0, 0.0, 0.11200000345707, 42.0, 0.49479165673256, 0.11200000345707, 0.0, 0.0, 0.3120000064373, 36.0, 0.39583334326744, 0.10800000280142, 0.0, 0.0, 0.19733333587646, 36.0, 0.390625, 0.21333333849907, 0.0, 0.0, 0.17733334004879, 36.0, 0.38541665673256, 1.1879999637604, 0.0, 0.0, 0.2986666560173, 42.0, 0.61458331346512, 0.094666667282581, 0.0, 0.0, 0.3906666636467, 40.0, 0.5, 0.23333333432674, 0.0, 0.0, 0.2986666560173, 36.0, 0.48958334326744, 0.082666665315628, 0.0, 0.0, 0.16666667163372, 36.0, 0.43229165673256, 0.26933333277702, 0.0, 0.0 ]
    ];

    data = [
      FloatArray[ 0.54666668176651, 0.0, 0.0, 0.39333334565163, 36.0, 0.46875, 0.19733333587646, 0.0, 0.0, 0.195999994874, 36.0, 0.40104165673256, 1.1786667108536, 0.0, 0.0, 0.27599999308586, 42.0, 0.5, 0.10800000280142, 0.0, 0.0, 0.4040000140667, 40.0, 0.421875, 0.4066666662693, 42.0, 0.64583331346512, 0.41200000047684, 36.0, 0.51041668653488, 0.27066665887833, 0.0, 0.0, 0.16266666352749, 36.0, 0.41666665673256, 0.33599999547005, 0.0, 0.0, 0.24400000274181, 42.0, 0.55208331346512, 0.53600001335144, 0.0, 0.0, 0.28666666150093, 42.0, 0.578125, 0.15199999511242, 0.0, 0.0, 0.37200000882149, 40.0, 0.52604168653488, 0.41333332657814, 42.0, 0.52604168653488, 0.0040000001899898, 0.0, 0.0, 0.44666665792465, 36.0, 0.53125 ],
      FloatArray[ 0.93999999761581, 0.0, 0.0, 0.19733333587646, 42.0, 0.484375, 0.17066666483879, 0.0, 0.0, 0.36133334040642, 40.0, 0.48958334326744, 0.0253333337605, 0.0, 0.0, 0.11200000345707, 42.0, 0.49479165673256, 0.11200000345707, 0.0, 0.0, 0.3120000064373, 36.0, 0.39583334326744, 0.10800000280142, 0.0, 0.0, 0.19733333587646, 36.0, 0.390625, 0.21333333849907, 0.0, 0.0, 0.17733334004879, 36.0, 0.38541665673256, 1.1879999637604, 0.0, 0.0, 0.2986666560173, 42.0, 0.61458331346512, 0.094666667282581, 0.0, 0.0, 0.3906666636467, 40.0, 0.5, 0.23333333432674, 0.0, 0.0, 0.2986666560173, 36.0, 0.48958334326744, 0.082666665315628, 0.0, 0.0, 0.16666667163372, 36.0, 0.43229165673256, 0.26933333277702, 0.0, 0.0 ]
    ];

    data = data.collect {|a| a[0..11]};

    this.analyze;
  }

  test_typed {
  
    data = [
      FloatArray[
        0.5, 32, 0.5,
        1, 32, 0.5,
        1.5,0,0,
        0.5,32,0
      ],
      FloatArray[
        0.5, 0, 0,
        1.5, 40, 0.5,
        1,0,0,
        1.5,40, 0
      ],
      FloatArray[
        0.5, 0, 0,
        2, 42, 0.5,
        0.5, 0, 0,
        1, 42, 0
      ]
    ];

    this.analyze;
 
    this.assertSections([ false, 0, true, 0.5, false, 2.5, true, 3, false, 4.5 ]);
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

