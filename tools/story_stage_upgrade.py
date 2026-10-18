"""Rebuild larger story stages and guaranteed bilingual side-site field notes.
Run: python -X utf8 tools/story_stage_upgrade.py
Original authored cores are retained alongside this script, without local build dependencies.
"""
import json
import shutil
from pathlib import Path
import template_io
from story_island_exploration import ROOT, SIZES, expand_templates
from blend_island_coasts import refine, OUTPUT

PROJECT = Path(__file__).resolve().parents[1]
# Four accounts per chapter follow Mara, Ivo and Sela, without resolving the eastern signal.
ACCOUNTS = [
 [
  ("Mara: The spare watch cot was still warm when the mast fell. We carried the injured inland before returning for the receiver. Nobody was left on the landing.", "Mara: Das Ersatzbett war noch warm, als der Mast fiel. Wir brachten die Verletzten ins Inland, dann holten wir den Empfänger. Niemand blieb am Steg zurück."),
  ("Sela: Salt ruined the first seed tray. I washed the second with our drinking water. Ivo objected, then brought his own cup. Three green shoots survived.", "Sela: Salz verdarb die erste Saatschale. Die zweite wusch ich mit Trinkwasser. Ivo protestierte, dann brachte er seinen Becher. Drei grüne Triebe überlebten."),
  ("Ivo: One lamp answered beyond the headland. Its pauses were deliberate. I marked Glasswatch on the dry chart; the lookout may still have a working lens.", "Ivo: Hinter der Landzunge antwortete eine Lampe. Ihre Pausen waren absichtlich. Ich markierte die Glaswacht auf der trockenen Karte. Vielleicht ist ihre Linse noch heil."),
  ("Mara: We sheltered here through the second squall. Someone had scratched six names into the rock. Our passenger list has only three. I copied them before the tide rose.", "Mara: Hier überstanden wir die zweite Böe. Sechs Namen waren in den Fels geritzt. Unsere Liste hat nur drei. Ich schrieb sie vor der Flut ab.")
 ],
 [
  ("Mara: The keeper left blankets folded by size, with a child's pair of boots beneath them. We left the boots. We will ask every boat we meet about their owner.", "Mara: Die Decken des Wächters waren nach Größe gefaltet. Darunter standen Kinderstiefel. Wir ließen sie stehen. Jedes Boot werden wir nach ihrem Besitzer fragen."),
  ("Sela: The lookout crew tried growing beans in cracked lamp bowls. Their watering marks stop on the day the supply boat failed to arrive. I took seeds, not the bowls.", "Sela: Die Wache zog Bohnen in gesprungenen Lampenschalen. Die Gießstriche enden am Tag des vermissten Versorgungsboots. Ich nahm Samen mit, nicht die Schalen."),
  ("Ivo: The lens points toward Garden Relay. Its frame bears a fresh repair beneath old salt. Somebody has passed this way since the storm; our charts are already late.", "Ivo: Die Linse zeigt zum Gartenrelais. Unter altem Salz ist eine frische Reparatur. Seit dem Sturm war jemand hier. Unsere Karten sind bereits veraltet."),
  ("Mara: Under the rock shelf lay a torn list, wrapped in oilcloth. It mentions repairs we have not made. Ivo says it must be a misdated copy. I want to believe him.", "Mara: Unter dem Fels lag eine eingerissene Liste in Öltuch. Sie nennt Reparaturen, die wir noch nicht machten. Ivo hält sie für falsch datiert. Ich möchte ihm glauben.")
 ],
 [
  ("Sela: There was room for us in the greenhouse, but not enough clean water for everyone. We repaired the gutter before unpacking. That evening nobody counted cups.", "Sela: Im Gewächshaus war Platz für uns, aber zu wenig sauberes Wasser. Vor dem Auspacken reparierten wir die Rinne. Abends zählte niemand mehr die Becher."),
  ("Sela: Keep the beans above the salt line. The ledger records what each crew planted, not what they owed. Leave one row unharvested for whoever lands next.", "Sela: Die Bohnen müssen über der Salzlinie bleiben. Das Buch nennt die Aussaat jeder Mannschaft, keine Schulden. Lasst eine Reihe für die nächsten Besucher stehen."),
  ("Ivo: A gardener traced the route to Echo Vault in charcoal. The shore markers are water heights, not graves. I had followed the wrong line until she corrected me.", "Ivo: Eine Gärtnerin zeichnete den Weg zum Echogewölbe mit Kohle. Die Ufermarken sind Wasserstände, keine Gräber. Ohne sie wäre ich der falschen Linie gefolgt."),
  ("Mara: The archive seal needs a key and three switches: left on, middle off, right on. I copied the sequence twice. Leave the key with our main journal for the next crew.", "Mara: Das Archiv braucht Schlüssel und drei Schalter: links an, Mitte aus, rechts an. Ich schrieb es zweimal ab. Der Schlüssel bleibt beim Hauptjournal.")
 ],
 [
  ("Mara: We dried the wet charts one sheet at a time. Every route has a crew name beside it. The smallest boat crossed the longest stretch; its water ration was shared.", "Mara: Wir trockneten die Karten Blatt für Blatt. Jede Route trägt einen Mannschaftsnamen. Das kleinste Boot schaffte die längste Strecke. Sein Wasser wurde geteilt."),
  ("Sela: The vault garden was planted in broken crates. Even here someone kept seed for another season. I exchanged our last beans for a packet marked 'harbor soil'.", "Sela: Der Gewölbegarten wächst in kaputten Kisten. Auch hier sparte jemand Saat für die nächste Saison. Unsere letzten Bohnen tauschte ich gegen Hafensaat."),
  ("Ivo: Ember Beacon appears in the margins of three unrelated charts. Its light stayed on after their other landmarks vanished. I will trust a kept watch before an old bearing.", "Ivo: Glutfeuer steht am Rand dreier fremder Karten. Sein Licht blieb, als andere Marken verschwanden. Einer gehaltenen Wache vertraue ich mehr als einer alten Peilung."),
  ("Mara: The loose list matches a patch we made this morning. I placed both records in the dry case. We will keep the originals; guessing is easier than admitting uncertainty.", "Mara: Die lose Liste nennt unseren Flicken von heute Morgen. Beide Aufzeichnungen liegen jetzt trocken. Die Originale bleiben. Raten ist leichter, als Zweifel zuzugeben.")
 ],
 [
  ("Mara: The beacon keeper slept in short watches, boots beside the door. We rebuilt the shelter roof while Ivo repaired the feed wire. For one night the keeper slept.", "Mara: Der Leuchtfeuerwächter schlief nur kurz, die Stiefel an der Tür. Wir deckten das Dach, Ivo flickte den Draht. Eine Nacht lang konnte der Wächter schlafen."),
  ("Sela: Ash makes poor soil unless mixed carefully. The keeper showed me the surviving herb bed behind the windbreak. We left fresh seed and took only what was ready.", "Sela: Asche allein ist schlechter Boden. Hinter dem Windschutz zeigte der Wächter sein Kräuterbeet. Wir ließen frische Saat und nahmen nur reife Pflanzen."),
  ("Ivo: From here the harbor cranes look like broken antennae. Tideworks Haven still answers on the work channel. They ask for tools and tank patches, never payment.", "Ivo: Die Hafenkräne sehen wie kaputte Antennen aus. Die Gezeitenwerft antwortet noch im Arbeitskanal. Sie bittet um Werkzeug und Tankflicken, nie um Bezahlung."),
  ("Mara: The list reached this shore before we did. The keeper remembers dry paper on a rain-soaked boat, but not its captain's face. We have a witness, not an answer.", "Mara: Die Liste war vor uns hier. Der Wächter erinnert sich an trockenes Papier auf einem regennassen Boot, nicht an den Kapitän. Ein Zeuge ist noch keine Antwort.")
 ],
 [
  ("Mara: Each repaired berth took another stranded crew. We widened ours before building a bed. At supper there were more voices than bowls, and nobody ate alone.", "Mara: Jeder reparierte Liegeplatz nahm eine weitere Mannschaft auf. Vor dem Bett bauten wir unseren Steg breiter. Beim Essen waren es mehr Stimmen als Schalen."),
  ("Sela: This bed uses soil carried from four islands. The harbor ledger records who brought each sack. I planted one row for our return and another for strangers.", "Sela: Dieses Beet enthält Erde von vier Inseln. Im Hafenbuch steht, wer jeden Sack brachte. Eine Reihe säte ich für unsere Rückkehr, eine für Fremde."),
  ("Ivo: The northern observatory sent a bearing without a name. The corrected charts are stored at Starfall Archive. Bring the harbor key; the outer seal is still intact.", "Ivo: Das nördliche Observatorium sendete eine namenlose Peilung. Die berichtigten Karten liegen im Sternenarchiv. Nehmt den Hafenschlüssel; das äußere Siegel hält noch."),
  ("Mara: Archive switches: left on, middle off, right on, then use the key. The copy stays in our main journal too. Nobody should lose the route because one page gets wet.", "Mara: Archivschalter: links an, Mitte aus, rechts an, dann den Schlüssel benutzen. Die Folge steht auch im Hauptjournal. Eine nasse Seite darf den Weg nicht versperren.")
 ],
 [
  ("Mara: We packed the dry charts but left copies in the shelter. Someone will follow our lamps. If they find these pages, they should know we chose to sail together.", "Mara: Die trockenen Karten sind gepackt, Kopien bleiben im Schutzhaus. Jemand wird unseren Lampen folgen. Diese Seiten sollen sagen: Wir segelten gemeinsam weiter."),
  ("Sela: The observatory garden has enough seed for a second planting. I could stay. Instead I filled the traveling box and left the rest beside the watering cup.", "Sela: Im Observatorium liegt Saat für eine zweite Aussaat. Ich könnte bleiben. Stattdessen füllte ich die Reisekiste. Der Rest bleibt beim Gießbecher."),
  ("Ivo: The eastern voice returned after our own beacon went dark. It cannot be a simple echo. I marked the time but left the destination blank. We need another observation.", "Ivo: Die östliche Stimme kam wieder, als unser Feuer aus war. Ein einfaches Echo ist es nicht. Die Zeit ist notiert, das Ziel bleibt leer. Eine weitere Messung fehlt."),
  ("Mara: 'Bring the register' may be an instruction or a warning. We have no reliable coordinates beyond this archive. Keep listening; do not draw a safe harbor where we know only sea.", "Mara: 'Bringt das Verzeichnis' mag Auftrag oder Warnung sein. Hinter dem Archiv kennen wir keine sicheren Koordinaten. Hört weiter zu. Zeichnet keinen Hafen in unbekannte See.")
 ]
]


def main():
    for name in SIZES:
        shutil.copyfile(Path(__file__).with_name('story_cores')/f'{name}.nbt', ROOT/f'structure/islands/{name}.nbt')
    expand_templates()
    # Reapply the established terrain grading, preserving architecture and route headroom.
    for name in SIZES:
        path=ROOT/f'structure/islands/{name}.nbt'
        refine(path)
        shutil.copyfile(OUTPUT/f'data/raft_ocean/structure/islands/{name}.nbt',path)
    add_field_notes()


def add_field_notes():
    report=json.loads((ROOT/'story_island_exploration.json').read_text(encoding='utf-8'))
    languages={locale:json.loads((PROJECT/f'src/main/resources/assets/raft_ocean/lang/{locale}.json').read_text(encoding='utf-8')) for locale in ('en_us','de_de')}
    for stage,name in enumerate(SIZES):
        path=ROOT/f'structure/islands/{name}.nbt'
        root=template_io.read(path);blocks=template_io.blocks(root)
        for index,site in enumerate(report[name]['sites']):
            key=f'raft_ocean.story.field.{stage}.{index}'
            note_table=f'raft_ocean:chests/field_notes/{name}_{index}'
            x,y,z=site['pos']
            containers=[(pos,state) for pos,state in blocks.items() if abs(pos[0]-x)<=3 and pos[1]==y and abs(pos[2]-z)<=3 and state[2] and state[2].get('LootTable') in [(8,'raft_ocean:chests/story_side_supplies'),(8,note_table)]]
            assert len(containers)==1,(name,site['kind'],containers)
            containers[0][1][2]['LootTable']=(8,note_table)
            for language in languages:
                en=language=='en_us'
                languages[language][key+'.title']=('Field note' if en else 'Feldnotiz')+f' {stage+1}.{index+1}'
                languages[language][key+'.page']=ACCOUNTS[stage][index][0 if en else 1]
                languages[language][key+'.context']=('Recovered at '+site['kind'].replace('_',' ')+'. The main island journal carries the next signal bearing. This page adds testimony, not a new destination.' if en else 'Hier blieb ein Bericht unserer Mannschaft zurück. Die nächste Peilung steht im Hauptjournal der Insel. Diese Seite ergänzt die Geschichte, ohne ein neues Ziel zu nennen.')
            entry={'type':'minecraft:item','name':'minecraft:written_book','modifier':[{'type':'minecraft:set_components','components':{
                'minecraft:custom_name':{'translate':key+'.title'},
                'minecraft:written_book_content':{'title':f'Field note {stage+1}.{index+1}','author':'Mara, Ivo & Sela','resolved':True,'pages':[{'raw':{'translate':key+'.page'}},{'raw':{'translate':key+'.context'}}]}}}]}
            loot={'type':'minecraft:chest','pools':[{'rolls':1,'entries':[{'type':'minecraft:loot_table','value':'raft_ocean:chests/story_side_supplies'}]},{'rolls':1,'entries':[entry]}]}
            target=ROOT/f'loot_table/chests/field_notes/{name}_{index}.json';target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(loot,indent=2)+'\n',encoding='utf-8')
            site['note']=key;site['lootTable']=note_table
        root['RaftOceanStageVersion']=(3,1)
        template_io.replace_blocks(root,blocks);template_io.write(path,root)
    for locale,language in languages.items():
        (PROJECT/f'src/main/resources/assets/raft_ocean/lang/{locale}.json').write_text(json.dumps(language,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    (ROOT/'story_island_exploration.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print('Seven increasing story stages and 28 guaranteed bilingual field-note books generated.')

if __name__=='__main__':
    main()
