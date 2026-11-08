@echo off
rem Moves the removed Soundpad module and its sounds into _to_delete (delete that folder afterwards).
cd /d "%~dp0"
if not exist _to_delete mkdir _to_delete
move /Y "ru\recoin\implement\features\modules\misc\Soundpad.java" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_alarm.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_backup.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_congrats.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_correct.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_cover_me.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_deathmatch.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_dice.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_explosion.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_fight.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_final_round.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_fire_in_the_hole.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_go_go_go.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_hurry_up.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_jingle_pizzi.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_jingle_steel.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_laser_0.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_laser_1.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_laser_2.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_level_up.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_medic.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_prepare.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_ready.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_reloading.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_six_seven.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_slime_0.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_slime_1.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_sniper.ogg" _to_delete\ >nul 2>&1
move /Y "src\main\resources\assets\recoin\sounds\events\pad_wrong.ogg" _to_delete\ >nul 2>&1
echo Done.
pause
