import { GrpcWebFetchTransport } from "@protobuf-ts/grpcweb-transport";
import { PayToWinClient } from "./generated/paytowin.client";
import {
  DatalessEffect,
  DiamondTool,
  EffectRequest,
  MinecraftEntityWrapper_MinecraftEntity,
  MinecraftMaterialWrapper_MinecraftMaterial,
  PlayersRequest,
  PotionNameWrapper_PotionName,
} from "./generated/paytowin";
import { useEffect, useState } from "react";
import "./index.css";
import ReactSelect from "react-select";

function App() {
  const transport: GrpcWebFetchTransport = new GrpcWebFetchTransport({
    baseUrl: "http://localhost:8080",
    meta: {
      password: "assword",
    },
  });

  const client: PayToWinClient = new PayToWinClient(transport);

  const [playersOnline, setPlayersOnline] = useState<string[]>([] as string[]);

  const [selectedPlayer, selectPlayer] = useState<string>("");

  const [selectedEffect, selectEffect] = useState<string>("");

  const [selectedPotionEffect, selectPotionEffect] = useState<PotionNameWrapper_PotionName>();

  const [selectedMiscEffect, selectMiscEffect] = useState<DatalessEffect>();

  const [selectedToolEffect, selectToolEffect] = useState<DiamondTool>();

  const [selectedItemEffect, selectItemEffect] = useState<MinecraftMaterialWrapper_MinecraftMaterial>();

  const [itemAmount, selectItemAmount] = useState<number>();

  const [selectedEntityEffect, selectEntityEffect] =
    useState<MinecraftEntityWrapper_MinecraftEntity>();

  const [entityAmount, selectEntityAmount] = useState<number>();

  const [requester, setRequester] = useState<string>();

  const [effectType, changeEffectType] =
    useState<EffectRequest["effect"]["oneofKind"]>(undefined);

  useEffect(() => {
    const playersResponse = client.getPlayers(PlayersRequest.create());

    (async () => {
      try {
        for await (const { players } of playersResponse.responses) {
          console.log(players);
          setPlayersOnline(players);
        }
      } catch (error) {
        console.log(error);
      }
    })();
  }, []);

  const applyMiscEffect = (player: string, effect: DatalessEffect): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          dataless: effect,
          oneofKind: "dataless",
        },
        player: player,
        requester: requester,
      }),
    );
  };

  const applyToolEffect = (player: string, effect: DiamondTool): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          tool: effect,
          oneofKind: "tool",
        },
        player: player,
        requester: requester,
      }),
    );
  };

  const applyPotionEffect = (player: string, effect: PotionNameWrapper_PotionName): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          potion: {
            name: effect,
            duration: 60,
            amplifier: 3,
          },
          oneofKind: "potion",
        },
        player: player,
        requester: requester,
      }),
    );
  };

  const applyItemEffect = (
    player: string,
    effect: MinecraftMaterialWrapper_MinecraftMaterial,
    amount: number,
  ): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          item: {
            itemName: effect,
            amount: amount,
          },
          oneofKind: "item",
        },
        player: player,
        requester: requester,
      }),
    );
  };

  const applyEntityEffect = (
    player: string,
    effect: MinecraftEntityWrapper_MinecraftEntity,
    amount: number,
  ): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          spawnEntity: {
            entity: effect,
            amount: amount,
          },
          oneofKind: "spawnEntity",
        },
        player: player,
        requester: requester,
      }),
    );
  };

  const sendToHeaven = (player: string): void => {
    client.applyEffect(
      EffectRequest.create({
        effect: {
          potion: {
            name: PotionNameWrapper_PotionName.LEVITATION,
            duration: 1,
            amplifier: 100,
          },
          oneofKind: "potion",
        },
        player: player,
        requester: requester,
      }),
    );
  };

  const applyEffect = (): void => {
    if (selectedPlayer == "") return;
    if (selectedEffect == "Send to heaven") {
      sendToHeaven(selectedPlayer);
      alert("success");
      return;
    }
    if (effectType == "potion" && selectedPotionEffect != undefined) {
      applyPotionEffect(selectedPlayer, selectedPotionEffect);
    }
    if (effectType == "dataless" && selectedMiscEffect != undefined) {
      applyMiscEffect(selectedPlayer, selectedMiscEffect);
    }
    if (effectType == "tool" && selectedToolEffect != undefined) {
      applyToolEffect(selectedPlayer, selectedToolEffect);
    }
    if (effectType == "item" && itemAmount && selectedItemEffect != undefined) {
      applyItemEffect(selectedPlayer, selectedItemEffect, itemAmount);
    }
    if (
      effectType == "spawnEntity" &&
      entityAmount &&
      selectedEntityEffect != undefined
    ) {
      applyEntityEffect(selectedPlayer, selectedEntityEffect, entityAmount);
    }

    alert("success");
  };

  type Effect = EffectRequest['effect'];
  type ExtractOneOfKind<T> = T extends { oneofKind: infer U } ? U : never;
  type EffectTypes = ExtractOneOfKind<Effect>;

  type ValueLabelPair<T> = {
    value: T;
    label: string;
  };

  const effectMapping: ValueLabelPair<EffectTypes>[] = [
    { value: "potion", label: "Potion effect" },
    { value: "dataless", label: "Miscellaneous effect" },
    { value: "tool", label: "Give a diamond tool" },
    { value: "item", label: "Give a certain amount of items" },
    { value: "spawnEntity", label: "Spawn a certain amount of entities" },
  ];

  const listOfPotionEffects = Object.entries(PotionNameWrapper_PotionName)
    .filter(([, value]) => typeof value === "number")
    .map(([key, value]) => ({
      value: value as PotionNameWrapper_PotionName,
      label: key.toLowerCase().replace(/[_]+/g, " ").split(" ").map((s) => s.charAt(0).toUpperCase() + s.slice(1)).join(" "),
    }));

  const listOfMiscEffects = Object.entries(DatalessEffect)
    .filter(([, value]) => typeof value === "number")
    .map(([key, value]) => ({
      value: value as DatalessEffect,
      label: key,
    }));

  const listOfToolEffects = Object.entries(DiamondTool)
    .filter(([, value]) => typeof value === "number")
    .map(([key, value]) => ({
      value: value as DiamondTool,
      label: key,
    }));

  const listOfItemEffects = Object.entries(MinecraftMaterialWrapper_MinecraftMaterial)
    .filter(([, value]) => typeof value === "number")
    .map(([key, value]) => ({
      value: value as MinecraftMaterialWrapper_MinecraftMaterial,
      label: key.toLowerCase().replace(/[_]+/g, " ").split(" ").map((s) => s.charAt(0).toUpperCase() + s.slice(1)).join(" "),
    }));

  const itemAmountList = [
    { value: 1, label: 1 },
    { value: 4, label: 4 },
    { value: 16, label: 16 },
    { value: 32, label: 32 },
    { value: 64, label: 64 },
  ];

  const listOfEntityEffects = Object.entries(MinecraftEntityWrapper_MinecraftEntity)
    .filter(([, value]) => typeof value === "number")
    .map(([key, value]) => ({
      value: value as MinecraftEntityWrapper_MinecraftEntity,
      label: key.toLowerCase().replace(/[_]+/g, " ").split(" ").map((s) => s.charAt(0).toUpperCase() + s.slice(1)).join(" "),
    }));

  const entityAmountList = [
    { value: 1, label: 1 },
    { value: 2, label: 2 },
    { value: 4, label: 4 },
    { value: 8, label: 8 },
    { value: 16, label: 16 },
  ];

  return (
    <>
      <div className="text-xl font-bold m-2">Koornbeurs Minecraft LAN</div>
      <div className="m-2 p-2 border-t-2 border-b-2 border-black border-solid bg-gray-200">
        Requester:{" "}
        <input
          className="outline"
          type="text"
          onChange={(event) => setRequester(event.target.value)}
        ></input>
      </div>
      <div className="m-2">
        Selected player:{" "}
        {selectedPlayer == "" ? (
          "None"
        ) : (
          <div className="font-bold">{selectedPlayer}</div>
        )}
      </div>
      <div className="m-2 p-2 border-t-2 border-b-2 border-black border-solid bg-gray-200">
        Players online:
          <ReactSelect 
            options = {playersOnline.map((player: string) => {
              return { value: player, label: player }
            })}
            onChange = {(newValue): void => {
              if (newValue) {
                selectPlayer(newValue.value);
              }
            }}
          />
      </div>
      <div className="m-2">
        Selected effect:{" "}
        {selectedEffect == "" || !effectType || !effectMapping ? (
          "None"
        ) : selectedEffect == "Send to heaven" ? (
          <div className="font-bold">{"Special effect: " + selectedEffect}</div>
        ) : (
          <div className="font-bold">{effectMapping.find(x => x.value == effectType)!.label + ": " + selectedEffect}</div>
        )}
      </div>
      <div className="m-2">
        Effects:
        <div className="flex flex-col p-2 bg-gray-200 border-black border-t-2 border-t-solid border-b-2 border-b-solid">
        
          <div>
            What effect type would you like to select:
            <ReactSelect
              options={effectMapping}
              onChange={(newValue): void => {
                if (newValue) {
                  console.log(newValue);
                  changeEffectType(newValue.value);
                  selectEffect("");
                }
              }}
            />
          </div>
          {effectType == "dataless" ? 
            (<div>
              Some misc effects:
              <ReactSelect
                options={listOfMiscEffects}
                onChange={(newValue): void => {
                  if (newValue) {
                    console.log(newValue.value);
                    selectMiscEffect(newValue.value);
                    selectEffect(newValue.label);
                    changeEffectType("dataless");
                  }
                }}
              />
            </div>) : effectType == "item" ?
            (<div className="flex flex-row w-full m-2">
            <div className="w-1/4">Give a certain amount of items &#40;Tappers we trust you to not give away 32 diamond blocks for 1 beer or similar!&#41; :</div> <div>Item:</div>
            <div className="w-1/4 m-2">
              <ReactSelect
                options={listOfItemEffects}
                onChange={(newValue): void => {
                  if (newValue) {
                    console.log(newValue.value);
                    selectItemEffect(newValue.value);
                    selectEffect(newValue.label);
                    changeEffectType("item");
                  }
                }}
                
              />
            </div>
            Amount:
            <div className="w-1/4 m-2">
              <ReactSelect
                options={itemAmountList}
                onChange={(newValue): void => {
                  if (newValue) {
                    console.log(newValue.value);
                    selectItemAmount(newValue.value);
                  }
                }}
              />
            </div>
          </div>) : effectType == "potion" ?
          (<div>
            Specific potion effects (mild, and 60 seconds):
            <ReactSelect
              options={listOfPotionEffects}
              onChange={(newValue): void => {
                if (newValue) {
                  console.log(newValue.value);
                  selectPotionEffect(newValue.value);
                  selectEffect(newValue.label);
                  changeEffectType("potion");
                }
              }}
            />
          </div>) : effectType == "tool" ?
          (<div>
            Give a diamond item:
            <ReactSelect
              options={listOfToolEffects}
              onChange={(newValue): void => {
                if (newValue) {
                  console.log(newValue.value);
                  selectToolEffect(newValue.value);
                  selectEffect(newValue.label);
                  changeEffectType("tool");
                }
              }}
            />
          </div>) : effectType == "spawnEntity" ?
          (<div className="flex flex-row">
          Spawn a certain amount of mobs: Mob:
          <div className="w-1/4 m-2">
            <ReactSelect
              options={listOfEntityEffects}
              onChange={(newValue): void => {
                if (newValue) {
                  console.log(newValue.value);
                  selectEntityEffect(newValue.value);
                  selectEffect(newValue.label);
                  changeEffectType("spawnEntity");
                }
              }}
            />
          </div>
          Amount:
          <div className="w-1/4 m-2">
            <ReactSelect
              options={entityAmountList}
              onChange={(newValue): void => {
                if (newValue) {
                  console.log(newValue.value);
                  selectEntityAmount(newValue.value);
                }
              }}
            />
          </div>
        </div>) : <></>
          }
          
          
          
          
          
        </div>
        <div className="bg-gray-200 border-black border-b-2 border-b-solid">
          One more special effect that doesn't fall under the other types:
          <div
            className="bg-blue-400 hover:bg-blue-500 p-2 m-2 max-w-fit"
            onClick={() => selectEffect("Send to heaven")}
          >
            Send to heaven!
          </div>
        </div>
      </div>
      <div
        className="bg-red-400 hover:bg-red-500 p-2 m-2 max-w-fit"
        onClick={() => {
          applyEffect();
          selectEffect("");
          changeEffectType(undefined);
          selectPlayer("");
        }}
      >
        Apply the effect!
      </div>
    </>
  );

  
}

export default App;
