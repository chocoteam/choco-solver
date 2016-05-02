#!/bin/sh

# third create new packages
mkdir -p ./choco-solver/src/main/java/org/chocosolver/
mkdir -p ./choco-solver/src/test/java/org/chocosolver/
mkdir -p ./choco-samples/src/main/java/org/chocosolver/
mkdir -p ./choco-samples/src/test/java/org/chocosolver/

# fourth move old package to new ones
for dir in "memory" "samples" "solver" "util" ; do git mv ./choco-solver/src/main/java/${dir} ./choco-solver/src/main/java/org/chocosolver/${dir}; done
for dir in "choco" "memory" "util"
do
git mv ./choco-solver/src/test/java/${dir} ./choco-solver/src/test/java/org/chocosolver/${dir}
done

for dir in "explanations" "para" "propagation" "search" "variables" "DuplicateTest.java" "EnvironmentTest.java"
do
    git mv ./choco-solver/src/test/java/solver/${dir} ./choco-solver/src/test/java/org/chocosolver/solver/${dir}
done

for dir in "binary" "nary" "real" "ternary" "unary" "ConstraintTest.java" "DynamicPostTest.java" "DynamicPostTestFactory.java" "SetIntUnion.java"
do
    git mv ./choco-solver/src/test/java/solver/constraints/${dir} ./choco-solver/src/test/java/org/chocosolver/solver/constraints/${dir}
done


git mv ./choco-samples/src/main/java/samples ./choco-samples/src/main/java/org/chocosolver/samples

for dir in "docs" "samples" "util"; do
git mv ./choco-samples/src/test/java/${dir} ./choco-samples/src/test/java/org/chocosolver/${dir}
done


#854f295ab69037d6db940b09651c489d9f54202b
#a4064bb95be27d1aa7abf4dcb80c22fb93e7e9d5
#e493808d3fc0234d26781072e80005fe825af4d2
#335739490a2bb5b00b1ac8fa89cdf510c90bc2d3
#87c392496588256e7d710806094097369bcb7dbf
#7df85d417ab08954743410583d9fb19e6f2b02dc
#03344f17e19f3d170c1e37b72bff55f82756f41d
#68755af669d0164eaa9f0c920e1b55dcb0713227
#266d63870d376db77cdb3b4369a6507c7d0a0104
#1cfe114ab02e1e429ffec157a015f789663b7d2a
#279af1e6922a5ddab68d085fc1e64faa243eff86
#
#0e7075e25580060a592905611a597d00be41d865
#19294197db54a90b3d8d2dbc15d8f8ddb87ad865
#e12c3befd59fdd4f43d0a2e7487a4125a0171eb2
#
#de1a363ba9688a219d17c072f74bd55034f2ebe8
#59f3e18ca5905a67f85371c7669f48ca4199b11f
#bfcaeb843aeeb69a752e90c38ec9619f3928b09b
#5eb0ffa621384f9f839ae1fe58aa1baacd15a8be
#f34cdb78711c6748cdec36cd665e8c789a25fb4e
#4a6ba907c97df1202d41273d15967954b734a3b1
#e8e2485e1037afe2e1b63bf35f270f4919eafda8
#4c73ee6bdb40429be7703864bd36357ed3b46f43
#f645931454abb2ab163d965f8035eff2238820fa
#
#c3bceeeae57b557266af376dab23b9ea76330900
#e6453d180358b6ceb5b51cc92377f31f173d9f52
#82680f573e06d00df126f08a04895eee09b771f1
#
#a4043c11c180a216984a40384aff2ae17a91b0b0
#3b283379a0f9cfcb5e3414283dbd349569d25616
#1cfc620f365aaed64e0f7069d4dfe898756d87de
#1e8f928a2522004ceda1eba4b6e0df0f875efec4
#9559671ebced8fbb7e35438af33305bda37f0938
#81ed1849b007ae161f1ae37235fcdeb1d8dda95b
#10fb779a256444f32062a1f26a32292d9504cd5b
#97a1ded3f309c1996a165373c3630238135dd654
#8831445520fadc4c2fd9f91baa2a8e1def4ec276
#
#3e291cb7b52fd56e7452d26e4e7250589f3787fd
#
#a3c896b32ded8b7a4c3bcfaf42bcea40cfdefcc0
#
#c6a54e65b75f7c90541eb0ffacb1554499e2c2e7
#acc33fee2604cebfa5747555ce9983ef915c0ec7
#0d5d126ee3973fc163dc32456b4c6cca8668883a
#4a168da6c4c09b29469e98b820ef775ca3d6cbaa
#8a85faf4f1355261fd74a7808c67e98733d13a20
#21f743e278fbe5c31b106b095f921b180472dcfb
#0a005bd08852ea874e80018874fbd9e4feac402a
#491f4af7a5f7fc7b07adcc409ba72bea69d579b4
#8622ee2ba81d656f6ae1578dc3051f3922e6f072
#14229b998fccf31d7c6822901941b2da46254750
#c131ef0849f04cf635a23ac64c15821d2cf17099
#f621aa4021c305389b99194c4149f50c582696a8
#d0b979b3878ffa1f466a641ac95b245acf986450
#5bad928e516a240239c8697e54e01bb6ac2b8189
#1218124b51f733dd1d91d185d1c1f403cc260e2d
#d9d13fa8b3c5eddf2c0bfaec9cd1e63a681a8ff1
#a7d96ea804818a01338a1a3adf37ded236fa3a33
#652b78aecefb80eb562f55ff7aff075e5dbe1863
#
#bfb5a5aff9a443725cfe00d06d64a23e9355b23f
#eba93fca29efe9d01759dd355dba37a017308ed6